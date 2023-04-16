package app;

import Misc.Misc;
import Misc.Vector2d;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

public class Ray {
        /**
         * Координаты 1 точки
         */
        public final Vector2d pos1;
       /**
         * Координаты 2 точки
         */
        public final Vector2d pos2;
        /**
         * Конструктор точки
         *
         * @param pos1     положение точки 1
         * @param pos2     положение точки 2
         */
        @JsonCreator
        public Ray(@JsonProperty("pos1") Vector2d pos1, @JsonProperty("pos2") Vector2d pos2) {
            this.pos1 = pos1;
            this.pos2 = pos2;
        }


        /**
         * Получить цвет луча по её множеству
         *
         * @return цвет луча
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
//        public Vector2d getPos() {
//            return pos;
//        }

        /**
         * Получить радуис
         *
         * @return радиус
         */
//        public int getRadius() {
//            return radius;
//        }


        /**
         * Строковое представление объекта
         *
         * @return строковое представление объекта
         */
        @Override
        public String toString() {
            return "Point{" +
                    "pos1=" + pos1 +
                    ", pos2=" + pos2 +
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
            app.Ray ray = (app.Ray) o;
            return  Objects.equals(pos1, ray.pos1);
        }

        /**
         * Получить хэш-код объекта
         *
         * @return хэш-код объекта
         */
        @Override
        public int hashCode() {
            return Objects.hash( pos1, pos2);
        }


}
