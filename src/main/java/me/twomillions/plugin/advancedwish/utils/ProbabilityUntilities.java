package me.twomillions.plugin.advancedwish.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ProbabilityUntilities {
    // by https://www.spigotmc.org/threads/getting-random-key-from-hashmap-with-chance.199285/#post-2073868

    private List<Chance> chances;
    private int sum;
    private Random random;

    private class Chance {
        private int upperLimit;
        private int lowerLimit;
        private Object element;
        public Chance(Object element, int lowerLimit, int upperLimit) {
            this.element = element;
            this.upperLimit = upperLimit;
            this.lowerLimit = lowerLimit;
        }
        public int getUpperLimit() {
            return this.upperLimit;
        }
        public int getLowerLimit() {
            return this.lowerLimit;
        }
        public Object getElement() {
            return this.element;
        }
        public String toString() {
            return "[" + Integer.toString(this.lowerLimit) + "|" + Integer.toString(this.upperLimit) + "]: " + this.element.toString();
        }
    }
    public ProbabilityUntilities() {
        this.random = new Random();
        this.chances = new ArrayList<>();
        this.sum = 0;
    }

    public ProbabilityUntilities(long seed) {
        this.random = new Random(seed);
        this.chances = new ArrayList<>();
        this.sum = 0;
    }

    public void addChance(Object element, int chance) {
        if (this.chances.contains(element)) return;

        this.chances.add(new Chance(element, this.sum, this.sum + chance));
        this.sum = this.sum + chance;
    }

    public Object getRandomElement() {
        int index = this.random.nextInt(this.sum);
        for (Chance chance : this.chances) {
            if (chance.getLowerLimit() <= index && chance.getUpperLimit() > index) {
                return chance.getElement();
            }
        }
        return null;
    }

    public int getOptions() { // might be needed sometimes
        return this.sum;
    }
    public int getChoices() { // might be needed sometimes
        return this.chances.size();
    }
}
