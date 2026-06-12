package com.projetoagrinho.core;

import java.io.Serializable;
import java.util.*;

/**
 * GameCore.java
 * Lógica central do jogo: plantas, máquinas, agrotóxicos, alertas, pontos e roleta.
 * Implementação de exemplo em console; adaptar para Android/LibGDX/UI.
 */
public class GameCore implements Serializable {
    private static final long serialVersionUID = 1L;

    public enum Currency { DOLLAR, REAL }

    public static class Inventory {
        public double dollars = 0.0;
        public double reals = 0.0;
        public int seeds = 0;
        public int gems = 0;
    }

    public static class Plant {
        public String id;
        public boolean needsWater = true;
        public boolean hasPests = false;
        public boolean readyToHarvest = false;
        public Plant(String id) { this.id = id; }
    }

    public static class Machine {
        public String id;
        public Machine(String id) { this.id = id; }
        // Simula regar planta
        public void water(Plant p) {
            p.needsWater = false;
        }
        // Simula recolher planta
        public boolean collect(Plant p, Inventory inv) {
            if (p.readyToHarvest) {
                // recompensa simples
                inv.dollars += 1.0;
                p.readyToHarvest = false;
                return true;
            }
            return false;
        }
        // Usar agrotóxico para praga
        public void sprayPesticide(Plant p) {
            p.hasPests = false;
        }
    }

    public static class Alert {
        public String message;
        public Date timestamp;
        public Alert(String message) {
            this.message = message;
            this.timestamp = new Date();
        }
        @Override
        public String toString() {
            return "[" + timestamp + "] " + message;
        }
    }

    private List<Plant> plants = new ArrayList<>();
    private List<Machine> machines = new ArrayList<>();
    private List<Alert> alerts = new ArrayList<>();
    private Inventory inventory = new Inventory();
    private Random random = new Random();

    // Roleta: chances informadas pelo usuário: dolar/real 50%, sementes 25%, gemas 5%.
    // Restante 20% => "nada" (pode ser ajustado).
    public static class RouletteResult {
        public Currency currency;
        public double currencyAmount;
        public int seeds;
        public int gems;
        public String message;
    }

    public GameCore() {
        // Instancia duas máquinas por padrão
        machines.add(new Machine("waterer-01"));
        machines.add(new Machine("harvester-01"));

        // cria algumas plantas
        for (int i = 0; i < 10; i++) plants.add(new Plant("plant-" + i));
    }

    // Simulação de tempo: algumas plantas amadurecem ou recebem pragas
    public void simulateTick() {
        for (Plant p : plants) {
            if (!p.readyToHarvest && !p.needsWater) {
                // chance de amadurecer
                if (random.nextDouble() < 0.2) p.readyToHarvest = true;
            }
            // chance de praga aparecer
            if (!p.hasPests && random.nextDouble() < 0.05) {
                p.hasPests = true;
                addAlert("Praga detectada na " + p.id);
            }
            // se precisa de água por muito tempo, alertar
            if (p.needsWater && random.nextDouble() < 0.02) {
                addAlert("Planta " + p.id + " precisa de água!");
            }
        }
    }

    public void addAlert(String message) {
        Alert a = new Alert(message);
        alerts.add(a);
        // Aqui você pode disparar uma notificação externa (Android/Firebase) ao integrar.
        System.out.println("ALERTA: " + a);
    }

    public List<Alert> getAlerts() { return alerts; }
    public Inventory getInventory() { return inventory; }
    public List<Plant> getPlants() { return plants; }
    public List<Machine> getMachines() { return machines; }

    // Método para girar a roleta
    public RouletteResult spinRoulette() {
        double r = random.nextDouble();
        RouletteResult res = new RouletteResult();
        // Mapear probabilidades:
        // 0.00 - 0.50 => dólar/real (dividido entre DOLLAR e REAL aleatoriamente)
        // 0.50 - 0.75 => sementes
        // 0.75 - 0.80 => gems
        // 0.80 - 1.00 => nada
        if (r < 0.50) {
            // moeda
            if (random.nextBoolean()) {
                res.currency = Currency.DOLLAR;
                res.currencyAmount = round(random.nextDouble() * 5 + 1); // $1 a $6
                inventory.dollars += res.currencyAmount;
            } else {
                res.currency = Currency.REAL;
                res.currencyAmount = round(random.nextDouble() * 10 + 1); // R$1 a R$11
                inventory.reals += res.currencyAmount;
            }
            res.message = "Ganhou " + res.currencyAmount + " " + (res.currency == Currency.DOLLAR ? "dólares" : "reais");
        } else if (r < 0.75) {
            res.seeds = 1 + random.nextInt(5); // 1-5 sementes
            inventory.seeds += res.seeds;
            res.message = "Ganhou " + res.seeds + " sementes";
        } else if (r < 0.80) {
            res.gems = 1 + random.nextInt(2); // 1-2 gems
            inventory.gems += res.gems;
            res.message = "Ganhou " + res.gems + " gemas (rara)";
        } else {
            res.message = "Não ganhou nada. Tente novamente!";
        }
        return res;
    }

    private double round(double v) {
        return Math.round(v * 100.0) / 100.0;
    }

    // Exemplo de uso das máquinas: regar todas as plantas
    public void waterAllWithMachine() {
        Machine m = machines.get(0);
        for (Plant p : plants) m.water(p);
        addAlert("Máquina " + m.id + " regou todas as plantas");
    }

    // Colhe todas as plantas prontas
    public int collectAllWithMachine() {
        Machine m = machines.get(1);
        int collected = 0;
        for (Plant p : plants) {
            if (m.collect(p, inventory)) collected++;
        }
        if (collected > 0) addAlert("Máquina " + m.id + " colheu " + collected + " plantas");
        return collected;
    }

    // Usar agrotóxico em todas as plantas com pragas (consome sementes/agrotóxicos do inventário)
    public void sprayAllPests() {
        Machine m = machines.get(0);
        int sprayed = 0;
        for (Plant p : plants) {
            if (p.hasPests) {
                m.sprayPesticide(p);
                sprayed++;
            }
        }
        if (sprayed > 0) addAlert("Máquina " + m.id + " aplicou agrotóxico em " + sprayed + " plantas");
    }

    // Teste rápido em console
    public static void main(String[] args) throws InterruptedException {
        GameCore game = new GameCore();
        System.out.println("Inicializando jogo Projeto Agrinho (simulação console)");
        game.waterAllWithMachine();
        for (int i = 0; i < 10; i++) {
            game.simulateTick();
            if (i % 3 == 0) game.collectAllWithMachine();
            if (i % 4 == 0) game.spinRoulette();
            Thread.sleep(200);
        }
        System.out.println("Inventário final: $" + game.inventory.dollars + " | R$" + game.inventory.reals + " | Sementes: " + game.inventory.seeds + " | Gemas: " + game.inventory.gems);
        System.out.println("Alertas:");
        for (Alert a : game.getAlerts()) System.out.println(" - " + a);
    }
}
