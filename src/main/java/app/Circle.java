package app;

import Misc.Misc;
import Misc.Vector2d;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

public class Circle {

        /**
         * Координаты центра
         */
        public final Vector2d pos;
        /**
          * радиус
          */
        public final int radius;

        /**
         * Конструктор точки
         *
         * @param pos     положение точки
         * @param radius множество, которому она принадлежит
         */
        @JsonCreator
        public Circle(@JsonProperty("pos") Vector2d pos,@JsonProperty("pos") int radius) {
            this.pos = pos;
            this.radius = radius;
        }


        /**
         * Получить цвет точки по её множеству
         *
         * @return цвет точки
         */
        @JsonIgnore
        public int getColor() {
            return  Misc.getColor(0xCC, 0x00, 0x00, 0xFF);
        }

        /**
         * Получить положение
         * (нужен для json)
         *
         * @return положение
         */
        public Vector2d getPos() {
            return pos;
        }

    /**
     * Получить радуис
     *
     * @return радиус
     */
    public int getRadius() {
        return radius;
    }


        /**
         * Строковое представление объекта
         *
         * @return строковое представление объекта
         */
        @Override
        public String toString() {
            return "Point{" +
                    "radius=" + radius +
                    ", pos=" + pos +
                    '}';
        }

        /**
         * Проверка двух объектов на равенство
         *
         * @param o объект, с которым сравниваем текущий
         * @return флаг, равны ли два объекта
         */
        @Override
        public boolean equals(Object o) {
            // если объект сравнивается сам с собой, тогда объекты равны
            if (this == o) return true;
            // если в аргументе передан null или классы не совпадают, тогда объекты не равны
            if (o == null || getClass() != o.getClass()) return false;
            // приводим переданный в параметрах объект к текущему классу
            app.Circle circle = (app.Circle) o;
            return  Objects.equals(pos, circle.pos);
        }

        /**
         * Получить хэш-код объекта
         *
         * @return хэш-код объекта
         */
        @Override
        public int hashCode() {
            return Objects.hash( pos, radius);
        }

}
