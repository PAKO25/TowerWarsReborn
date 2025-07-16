package io.github.pako25.towerWars.Arena;

import java.util.ArrayList;
import java.util.Iterator;

public class MobQueue {
    private final ArrayList<QueuedMob> mobList = new ArrayList<>();

    public void add(TWMob mob) {
        mobList.add(new QueuedMob(mob));
    }

    public ArrayList<TWMob> tick() {
        ArrayList<TWMob> out = new ArrayList<>();
        Iterator<QueuedMob> iterator = mobList.iterator();
        while (iterator.hasNext()) {
            QueuedMob queuedMob = iterator.next();
            queuedMob.increaseStatus();
            if (queuedMob.status > 1) {
                out.add(queuedMob.mob);
                iterator.remove();
            }
        }
        return out;
    }

    public int size() {
        return mobList.size();
    }
}

class QueuedMob {
    public final TWMob mob;
    public int status = 0;

    public QueuedMob(TWMob mob) {
        this.mob = mob;
    }

    public void increaseStatus() {
        status++;
    }
}