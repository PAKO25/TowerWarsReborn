package io.github.pako25.towerWars.Arena.MobData;

import io.github.pako25.towerWars.Arena.MobType;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class MobState {
    private final int maxSummonCountForBonus = 300;
    private final Material material;
    private final String name;
    private final int cost;
    private final int health;
    private final double speed;
    private final int income;
    private final boolean isAdvanced;
    private int incomeToPrestige;
    private int incomeToUnlock;
    private MobType advancedForm;
    private final MobType mobType;
    private final EntityType entityType;
    private int summonCount = 0;
    private final boolean summonable;
    private final MobStates mobStates;

    public MobState(Material material, String name, int cost, int health, double speed, int income, boolean isAdvanced, MobType mobType, EntityType entityType, FileConfiguration cfg, boolean summonable, MobStates mobStates) {
        this.material = material;
        this.name = name;
        this.cost = cost;
        this.health = health;
        this.speed = speed;
        this.income = income;
        this.isAdvanced = isAdvanced;
        this.mobType = mobType;
        this.entityType = entityType;
        this.summonable = summonable;
        this.mobStates = mobStates;
        if (!isAdvanced) {
            advancedForm = MobType.valueOf(cfg.getString(mobType.name() + ".evolution"));
            incomeToPrestige = cfg.getInt(advancedForm.name() + ".cost");
            incomeToUnlock = (int) (cost * 0.2);
        }
    }

    public Material getMaterial() {
        return material;
    }

    public String getName() {
        return name;
    }

    public int getCost(int playerIncome) {
        int incomeEvolution = getIncomeEvolution(playerIncome);
        if (incomeEvolution == 0) return cost;
        return (int) (cost * Math.pow(1.7, incomeEvolution));
    }

    public int getHealth() {
        return (int) (health * mobStates.getPowerCreepHealthMultiplyer()) + getSummoningBonusHealth();
    }

    public double getSpeed() {
        return speed + getSummoningBonusSpeed();
    }

    public int getIncome(int playerIncome) {
        int incomeEvolution = getIncomeEvolution(playerIncome);
        if (incomeEvolution == 0) return income;
        return (int) (income * Math.pow(1.5, incomeEvolution)) + getSummoningBonusIncome();
    }

    public boolean isAdvanced() {
        return isAdvanced;
    }

    public int getIncomeToPrestige() {
        return incomeToPrestige;
    }

    public int getIncomeToUnlock() {
        return incomeToUnlock;
    }

    public MobType getAdvancedForm() {
        return advancedForm;
    }

    public MobType getMobType() {
        return mobType;
    }

    public int getSummonCount() {
        return summonCount;
    }

    public void incrementSummon() {
        summonCount++;
    }

    private int getIncomeEvolution(int playerIncome) {
        if (!isAdvanced) return 0;
        int evolutionByIncome = 0;
        int nextEvolutionRequierment = cost * 16;
        while (playerIncome > nextEvolutionRequierment) {
            evolutionByIncome++;
            nextEvolutionRequierment = (int) (nextEvolutionRequierment * 1.7);
        }
        return evolutionByIncome < 6 ? evolutionByIncome : 5;
    }

    public String getIncomeEvolutionText(int playerIncome) {
        int incomeEvolution = getIncomeEvolution(playerIncome);
        StringBuilder incomeEvolutionText = new StringBuilder(" ");
        for (int i = 0; i < incomeEvolution; i++) {
            incomeEvolutionText.append("★");
        }
        return incomeEvolutionText.toString();
    }

    public int getIncomeForNextEvolutionByIncome(int playerIncome) {
        if (!isAdvanced) return 0;
        int evolutionByIncome = getIncomeEvolution(playerIncome);
        if (evolutionByIncome == 5) return 0;

        int nextEvolutionRequierment = cost * 16;
        while (playerIncome > nextEvolutionRequierment) {
            nextEvolutionRequierment = (int) (nextEvolutionRequierment * 1.7);
        }
        return nextEvolutionRequierment;
    }

    public String getSummonedBonus() {
        String text = "";

        int bonusHealth = getSummoningBonusHealth();
        if (bonusHealth > 0) text = text + "+" + bonusHealth + " extra health//";

        float bonusSpeed = getSummoningBonusSpeed();
        if (bonusSpeed > 0) {
            BigDecimal bd = new BigDecimal(Float.toString(bonusSpeed));
            bd = bd.setScale(2, RoundingMode.HALF_UP);
            text = text + "+" + bd.toPlainString() + " extra speed//";
        }

        int bonusIncome = getSummoningBonusIncome();
        if (bonusIncome > 0) text = text + "+" + bonusIncome + " extra income//";

        float bonusHealing = getSummoningBonusHealingFactor();
        if (bonusHealing > 0) {
            BigDecimal bd = new BigDecimal(Float.toString(bonusHealing));
            bd = bd.setScale(2, RoundingMode.HALF_UP);
            text = text + "Extra healing: " + (int) (bd.floatValue() * 100) + "%" + "//";
        }

        float bonusSlow = getSummoningBonusSlowFactor();
        if (bonusSlow > 0) {
            BigDecimal bd = new BigDecimal(Float.toString(bonusSlow));
            bd = bd.setScale(2, RoundingMode.HALF_UP);
            text = text + "Extra slowness: " + (int) (bd.floatValue() * 100) + "%" + "//";
        }

        float bonusBlind = getSummoningBonusBlindFactor();
        if (bonusBlind > 0) {
            BigDecimal bd = new BigDecimal(Float.toString(bonusBlind));
            bd = bd.setScale(2, RoundingMode.HALF_UP);
            text = text + "Extra blindness: " + (int) (bd.floatValue() * 100) + "%" + "//";
        }

        float bonusTpDistance = getSummoningBonusTpDistanceFactor();
        if (bonusTpDistance > 0) {
            BigDecimal bd = new BigDecimal(Float.toString(bonusTpDistance));
            bd = bd.setScale(2, RoundingMode.HALF_UP);
            text = text + "Extra TP distance: " + (int) (bd.floatValue() * 100) + "%" + "//";
        }

        float bonusStunRange = getSummoningBonusStunRange();
        if (bonusStunRange > 0) {
            BigDecimal bd = new BigDecimal(Float.toString(bonusStunRange));
            bd = bd.setScale(2, RoundingMode.HALF_UP);
            text = text + "Extra stun range: " + (int) (bd.floatValue() * 100) + "%" + "//";
        }

        if (text.isEmpty()) text = "No bonus as of yet.";
        return text;
    }

    public EntityType getEntityType() {
        return entityType;
    }

    public boolean isSummonable() {
        return summonable;
    }

    private int getSummoningBonusHealth() {
        if (!(mobType == MobType.WITHER_SKELETON || mobType == MobType.GOLD_ZOMBIE || mobType == MobType.DIAMOND_ZOMBIE))
            return 0;
        float amplifier = (float) (Math.min(summonCount, maxSummonCountForBonus)) / maxSummonCountForBonus;
        return (int) (health * amplifier);
    }

    public float getSummoningBonusSpeed() {
        if (!(mobType == MobType.RAINBOW_SHEEP || mobType == MobType.RUNNING_IRON_GOLEM || mobType == MobType.MAD_COW))
            return 0;
        float amplifier = (float) (Math.min(summonCount, maxSummonCountForBonus)) / (maxSummonCountForBonus * 3);
        return (float) (speed * amplifier);
    }

    public int getSummoningBonusIncome() {
        if (!(mobType == MobType.PIGGY_BANK || mobType == MobType.MAD_COW)) return 0;
        float amplifier = (float) (Math.min(summonCount, maxSummonCountForBonus)) / (maxSummonCountForBonus * 3);
        return (int) (income * amplifier);
    }

    public float getSummoningBonusHealingFactor() {
        if (!(mobType == MobType.HIGH_PRIEST)) return 0;
        float factor = (float) (Math.min(summonCount, maxSummonCountForBonus)) / (maxSummonCountForBonus * 2);
        return factor;
    }

    public float getSummoningBonusSlowFactor() {
        if (!(mobType == MobType.SPIDER_JOCKEY)) return 0;
        float factor = (float) (Math.min(summonCount, maxSummonCountForBonus)) / (maxSummonCountForBonus * 2);
        return factor;
    }

    public float getSummoningBonusBlindFactor() {
        if (!(mobType == MobType.SQUID)) return 0;
        float factor = (float) (Math.min(summonCount, maxSummonCountForBonus)) / (maxSummonCountForBonus * 2);
        return factor;
    }

    public float getSummoningBonusTpDistanceFactor() {
        if (!(mobType == MobType.ENDERMITE)) return 0;
        float factor = (float) (Math.min(summonCount, maxSummonCountForBonus)) / (maxSummonCountForBonus);
        return factor;
    }

    public float getSummoningBonusStunRange() {
        if (!(mobType == MobType.CHARGED_CREEPER)) return 0;
        float factor = (float) (Math.min(summonCount, maxSummonCountForBonus)) / (maxSummonCountForBonus * 3);
        return factor;
    }
}