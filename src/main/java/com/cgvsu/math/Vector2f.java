package com.cgvsu.math;

// Это заготовка для собственной библиотеки для работы с линейной алгеброй
public class Vector2f {
    final float eps = 1e-7f;
    public float x, y;

    public Vector2f(final float x, final float y) {
        this.x = x;
        this.y = y;
    }

    public boolean equals(final Object obj) {
        //Если объект, с которым происходит сравнение, этим же объектом и является, то они равны. Сравнивает ссылки текущего объекта и принятого как аргумент.
        if (this == obj) return true;

        //Если принятый объект null или другого класса, то возвращает false, не равны. При o == null они не равны, т.к. у null не может быть метода equals. Следовательно основной объект не null.
        if (obj == null || getClass() != obj.getClass()) return false;

        Vector2f other = (Vector2f) obj;
        return Math.abs(x - other.x) < eps && Math.abs(y - other.y) < eps;
    }
    /*public boolean equals(Vector3f other) {
        // todo: желательно, чтобы eps была глобальная константа
        return Math.abs(x - other.x) < eps && Math.abs(y - other.y) < eps;
    }*/

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }
}
