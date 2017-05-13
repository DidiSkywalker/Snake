package de.corvin.snake;

public class Vec2 {

    public int x, y;

    public Vec2(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public Vec2 add(int x, int y) {
        this.x += x;
        this.y += y;
        return this;
    }

    public Vec2 sub(int x, int y) {
        this.x -= x;
        this.y -= y;
        return this;
    }

    public Vec2 add(Vec2 v) {
        this.x += v.x;
        this.y += v.y;
        return this;
    }

    public Vec2 sub(Vec2 v) {
        this.x -= v.x;
        this.y -= v.y;
        return this;
    }

    public void set(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void set(Vec2 v) {
        this.x = v.x;
        this.y = v.y;
    }

    public Vec2 mul(int i) {
        return new Vec2(x*i, y*i);
    }

    public boolean equal(int x, int y) {
        return this.x == x && this.y == y;
    }

    public boolean equal(Vec2 v) {
        return this.x == v.x && this.y == v.y;
    }

    public boolean hits(Vec2 v) {
        return (x == v.x && y == v.y);
    }
}
