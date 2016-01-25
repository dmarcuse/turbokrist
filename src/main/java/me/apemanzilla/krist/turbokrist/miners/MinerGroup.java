package me.apemanzilla.krist.turbokrist.miners;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a group of {@code Miner} objects, which can all be controlled
 * through a single object.
 *
 * @author apemanzilla
 * @see Miner
 */
public class MinerGroup extends Miner implements MinerListener {

    private List<Miner> miners = new ArrayList<Miner>();
    private boolean mining = false;
    private boolean stopOnSolution = true;

    /**
     * Creates a new {@code MinerGroup} with the specified miners.
     *
     * @param miners Optional {@link
     *               me.apemanzilla.krist.turbokrist.miners.Miner Miner} objects that will be
     *               added to the group.
     */
    public MinerGroup(Miner... miners) {
        for (Miner m : miners) {
            addMiner(m);
        }
    }

    public void setStopOnSolution(boolean stopOnSolution) {
        this.stopOnSolution = stopOnSolution;
    }

    @Override
    public String getName() {
        return "Miner Group";
    }

    @Override
    protected void preMining(String block, int work) {

    }

    @Override
    protected void startMining(String block, int work) {

    }

    @Override
    public void start(String block, int work) {
        for (Miner m : miners) {
            m.start(block, work);
        }
        mining = true;
    }

    @Override
    protected void stopMining() {

    }

    @Override
    public void stop() {
        mining = false;
        for (Miner m : miners) {
            if (m.isMining()) {
                m.stop();
            }
        }
    }

    @Override
    public boolean isMining() {
        return mining;
    }

    @Override
    public boolean hasSolution() {
        for (Miner m : miners) {
            if (m.hasSolution()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Solution getSolution() {
        for (Miner m : miners) {
            if (m.hasSolution()) {
                return m.getSolution();
            }
        }
        return null;
    }

    @Override
    public void reset() {
        for(Miner m : miners) {
            m.reset();
        }
    }

    @Override
    public double getAverageHashrate() {
        double hr = 0;
        for (Miner m : miners) {
            hr += m.getAverageHashrate();
        }
        if (hr > Math.pow(10, 15)) {
            return 0;
        }
        return hr;
    }

    @Override
    public double getRecentHashrate() {
        double hr = 0;
        for (Miner m : miners) {
            hr += m.getRecentHashrate();
        }
        if (hr > Math.pow(10, 12)) {
            return 0;
        }
        return hr;
    }

    @Override
    public void destroy() {
        for (Miner m : miners) {
            m.destroy();
        }
    }

    @Override
    public void blockSolved(Solution sol) {
        if (this.stopOnSolution) stop();
        solved(sol);
    }

    public void addMiner(Miner m) {
        miners.add(m);
        m.addListener(this);
    }

}
