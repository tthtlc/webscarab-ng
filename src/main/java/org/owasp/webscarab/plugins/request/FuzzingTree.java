/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.owasp.webscarab.plugins.request;

import java.io.*;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.owasp.webscarab.domain.FuzzingVariable;
import org.owasp.webscarab.domain.NamedValue;
import org.owasp.webscarab.plugins.request.FuzzingModel.FuzzingModes;

/**
 * @author Lpz
 */
public class FuzzingTree {

    int pathsCount = 1;
    int height = 0;
    ArrayList<FuzzingMultiNode> nodes = new ArrayList<FuzzingMultiNode>();
    FuzzingModel.FuzzingModes mode = FuzzingModel.FuzzingModes.DEPTH_FIRST;

    public FuzzingTree(FuzzingModel.FuzzingModes mode) {
        this.mode = mode;
    }

    public int getSiblings(int ind) {
        return nodes.get(ind).getSiblings();
    }

    public void addFuzzingVariable(FuzzingVariable fz) throws FileNotFoundException, IOException {
        Logger.getLogger(FuzzingTree.class.getName()).log(Level.INFO, "Adding fuzzing variable {0} from {1}", new Object[]{fz.getVariableName(), fz.getSourcePath()});

        FuzzingMultiNode fmm = new FuzzingMultiNode(fz);
        nodes.add(fmm);
        height++;
        if (fmm.getSiblings() != 0) {
            pathsCount *= fmm.getSiblings();
        }
    }

    public int getPathsCount() {
        return pathsCount;
    }

    public int getHeight() {
        return height;
    }

    public FuzzingModes getMode() {
        return mode;
    }

    public LinkedList<NamedValue> getNamedValueList(int i) {
        if (i >= this.getPathsCount()) {
            throw new IllegalArgumentException("Wrong argument for getEntryList");
        }
        LinkedList<NamedValue> list = new LinkedList<NamedValue>();
        for (int h = 0; h < this.getHeight(); ++h) {
            FuzzingMultiNode fmm = nodes.get(h);
            int ind = getIndex(fmm, h, i);
            NamedValue e = new NamedValue(fmm.getName(), fmm.getVarValue(ind));
            list.add(e);
        }
        return list;
    }

    private int getIndex(FuzzingMultiNode fmm, int ch, int i) {
        Random r = new Random();
        switch (getMode()) {
            case RANDOM:
                return r.nextInt(fmm.getSiblings());
            case BREADTH_FIRST:
                return i % fmm.getSiblings();
            case DEPTH_FIRST:
                if (ch == this.getHeight() - 1) {
                    return i % fmm.getSiblings();
                } else if (ch == 0) {
                    return (i / fmm.getSiblings()) % fmm.getSiblings();
                } else {
                    return (this.getPathsCount() / this.getSiblings(ch + 1)) % fmm.getSiblings();
                }
            default:
                throw new UnsupportedOperationException("unknown fuzzing mode");
        }
    }

    public void construct(ArrayList<FuzzingVariable> flist) {
        for (FuzzingVariable fv : flist) {
            try {
                this.addFuzzingVariable(fv);
            } catch (FileNotFoundException ex) {
                Logger.getLogger(FuzzingTree.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(FuzzingTree.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    class FuzzingMultiNode {

        int siblings = 0;
        ArrayList<String> values = new ArrayList<String>();
        String name = "";

        public String getName() {
            return name;
        }

        public int getSiblings() {
            return siblings;
        }

        public String getVarValue(int i) {
            return values.get(i);
        }

        public FuzzingMultiNode(FuzzingVariable fz) throws FileNotFoundException, IOException {
            this.name = fz.getVariableName();
            // command line parameter
            FileInputStream fstream = new FileInputStream(fz.getSourcePath());
            // Get the object of DataInputStream
            DataInputStream in = new DataInputStream(fstream);
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String strLine;
            while ((strLine = br.readLine()) != null) {
                if (!values.contains(strLine)) {
                    values.add(strLine);
                    siblings++;
                }
            }
            in.close();
        }
    }
}
