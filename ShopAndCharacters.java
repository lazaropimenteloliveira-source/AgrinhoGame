package com.projetoagrinho.core;

import java.io.Serializable;
import java.util.*;

/**
 * ShopAndCharacters.java
 * Lógica de loja e personagems com raridades e preços em dólar/real.
 * Contém 2 personagens gratuitos por nível e grupos com as quantidades solicitadas.
 */
public class ShopAndCharacters implements Serializable {
    private static final long serialVersionUID = 1L;

    public enum Rarity { FREE, COMMON, UNCOMMON, RARE, ULTRA_RARE, LEGENDARY, SPECIAL }

    public static class CharacterForSale {
        public String id;
        public String displayName;
        public Rarity rarity;
        public double priceDollars; // preço em dólar opcional
        public double priceReals;   // preço em reais opcional
        public boolean isUnlockedByDefault;

        public CharacterForSale(String id, String name, Rarity rarity, double priceDollars, double priceReals, boolean free) {
            this.id = id;
            this.displayName = name;
            this.rarity = rarity;
            this.priceDollars = priceDollars;
            this.priceReals = priceReals;
            this.isUnlockedByDefault = free;
        }

        @Override
        public String toString() {
            return displayName + " ("+rarity+") - $" + priceDollars + " / R$" + priceReals + (isUnlockedByDefault ? " [GRÁTIS]" : "");
        }
    }

    private List<CharacterForSale> catalog = new ArrayList<>();
    private Set<String> owned = new HashSet<>();
    private Random random = new Random();

    public ShopAndCharacters() {
        // 2 personagens gratuitos no nível
        for (int i = 0; i < 2; i++) {
            CharacterForSale c = new CharacterForSale("free-" + i, "Grátis " + (i+1), Rarity.FREE, 0.0, 0.0, true);
            catalog.add(c);
            owned.add(c.id);
        }
        // 25 comuns
        addBatch("common", 25, Rarity.COMMON, 0.99, 3.99);
        // 25 incomuns
        addBatch("uncommon", 25, Rarity.UNCOMMON, 1.99, 7.99);
        // 25 menos baratas (interpreto como "rare-ish" - manter como COMMON/UNCOMMON mix). Vou tratar como COMMON extra
        addBatch("lesscheap", 25, Rarity.COMMON, 0.49, 1.99);
        // 9 raras
        addBatch("rare", 9, Rarity.RARE, 4.99, 19.99);
        // 15 ultra-raras
        addBatch("ultra", 15, Rarity.ULTRA_RARE, 9.99, 39.99);
        // 10 lendárias
        addBatch("legend", 10, Rarity.LEGENDARY, 19.99, 79.99);
        // 50 especiais
        addBatch("special", 50, Rarity.SPECIAL, 0.05, 0.20); // "ultra muito muito menos baratas" -> preços baixos em microtransações
    }

    private void addBatch(String prefix, int count, Rarity rarity, double priceDollars, double priceReals) {
        for (int i = 0; i < count; i++) {
            String id = prefix + "-" + i;
            String name = rarity.name() + " " + (i+1);
            catalog.add(new CharacterForSale(id, name, rarity, priceDollars, priceReals, false));
        }
    }

    public List<CharacterForSale> listCatalog() { return catalog; }
    public boolean purchase(String charId, GameCore.Inventory inv) {
        for (CharacterForSale c : catalog) {
            if (c.id.equals(charId)) {
                if (c.isUnlockedByDefault) { owned.add(c.id); return true; }
                // tenta pagar em dólar primeiro
                if (inv.dollars >= c.priceDollars) {
                    inv.dollars -= c.priceDollars;
                    owned.add(c.id);
                    return true;
                }
                // tenta pagar em reais
                if (inv.reals >= c.priceReals) {
                    inv.reals -= c.priceReals;
                    owned.add(c.id);
                    return true;
                }
                return false;
            }
        }
        return false;
    }

    public boolean isOwned(String charId) { return owned.contains(charId); }

    // Exemplo de "roleta de personagem" que dá personagem aleatório com peso por raridade (mais raros = menos chance)
    public CharacterForSale randomCharacterDrop() {
        // pesos por raridade
        Map<Rarity, Integer> weight = new HashMap<>();
        weight.put(Rarity.FREE, 30);
        weight.put(Rarity.COMMON, 40);
        weight.put(Rarity.UNCOMMON, 20);
        weight.put(Rarity.RARE, 7);
        weight.put(Rarity.ULTRA_RARE, 2);
        weight.put(Rarity.LEGENDARY, 1);
        weight.put(Rarity.SPECIAL, 10);

        int totalWeight = 0;
        for (CharacterForSale c : catalog) totalWeight += weight.getOrDefault(c.rarity, 1);
        int r = random.nextInt(totalWeight);
        int cum = 0;
        for (CharacterForSale c : catalog) {
            cum += weight.getOrDefault(c.rarity, 1);
            if (r < cum) return c;
        }
        return catalog.get(0);
    }

    // Teste rápido console
    public static void main(String[] args) {
        ShopAndCharacters shop = new ShopAndCharacters();
        System.out.println("Catalogo total: " + shop.listCatalog().size() + " personagens");
        int shown = 0;
        for (CharacterForSale c : shop.listCatalog()) {
            System.out.println(" - " + c);
            if (++shown > 20) break;
        }
    }
}