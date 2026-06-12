package com.projetoagrinho.core;

import java.io.Serializable;

/**
 * PlayerCharacter.java
 * Representa o personagem controlável pelo jogador, com movimentos e poses.
 * Contém métodos de movimento para desktop (teclado) e mobile (touch).
 * Adapte a integração das poses para sua engine de render/anim (LibGDX, Android Canvas, etc).
 */
public class PlayerCharacter implements Serializable {
    private static final long serialVersionUID = 1L;

    public enum Pose { IDLE, WALK, RUN, HARVEST, WATER, CELEBRATE, SAD }
    private String name;
    private float x = 0, y = 0;
    private Pose pose = Pose.IDLE;
    private float speed = 2.0f; // unidades por tick

    public PlayerCharacter(String name, float startX, float startY) {
        this.name = name;
        this.x = startX;
        this.y = startY;
    }

    public String getName() { return name; }
    public float getX() { return x; }
    public float getY() { return y; }
    public Pose getPose() { return pose; }

    // Movimentos desktop: teclas WASD ou setas
    public void moveLeft() { x -= speed; pose = Pose.WALK; }
    public void moveRight() { x += speed; pose = Pose.WALK; }
    public void moveUp() { y += speed; pose = Pose.WALK; }
    public void moveDown() { y -= speed; pose = Pose.WALK; }

    // Para parar
    public void stop() { pose = Pose.IDLE; }

    // Movimento móvel por toque: mover em direção a (tx,ty)
    public void moveTo(float tx, float ty) {
        float dx = tx - x;
        float dy = ty - y;
        float dist = (float)Math.sqrt(dx*dx + dy*dy);
        if (dist < 0.1f) { x = tx; y = ty; stop(); return; }
        x += dx / dist * speed;
        y += dy / dist * speed;
        pose = Pose.WALK;
    }

    // Ações do personagem
    public void performWaterPose() { pose = Pose.WATER; }
    public void performHarvestPose() { pose = Pose.HARVEST; }
    public void celebrate() { pose = Pose.CELEBRATE; }
    public void becomeSad() { pose = Pose.SAD; }

    @Override
    public String toString() {
        return "Character " + name + " at (" + x + "," + y + ") pose=" + pose;
    }
}