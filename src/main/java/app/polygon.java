package app;


import io.github.humbleui.skija.Canvas;
import io.github.humbleui.skija.Paint;
import io.github.humbleui.skija.Point;
import Misc.Misc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Класс многоугольника
 */
public class polygon {
    /**
     * Список точек
     */
    private final List<Point> pointList;
    /**
     * Список цветов
     */
    private final List<Integer> colorList;
    /**
     * Массив цветов для рисования
     */
    private int[] renderColors;
    /**
     * Массив точек для рисования
     */
    Point[] renderPoints;

    /**
     * Конструктор многоугольника
     */
    public polygon() {
        pointList = new ArrayList<>();
        colorList = new ArrayList<>();
    }

    /**
     * Добавить точку в многоугольник
     *
     * @param x     координата X
     * @param y     координата Y
     * @param color цвет
     */
    public void add(int x, int y, int color) {
        pointList.add(new Point(x, y));
        colorList.add(color);
    }

    /**
     * Очистить точки
     *
     */
    public void clear() {
        pointList.clear();
        colorList.clear();
    }

    /**
     * Рассчитать точки и цвета для рисования
     */
    public void calculate() {
        // кол-во точек
        int n = pointList.size();
        // 2n нужно для хранения для каждого треугольника двух опорных точек
        // ещё один элемент(с индексом 0) нужен для хранения общей третьей опорной
        // точки
        renderColors = new int[n * 2 + 1];
        renderPoints = new Point[n * 2 + 1];

        // переменные для нахождения центра многоугольника
        float sumX = 0;
        float sumY = 0;

        // перебираем все вершины многоугольника кроме последней
        for (int i = 0; i < n - 1; i++) {
            // прибавляет координаты рассматриваемой точки к сумме
            sumX += pointList.get(i).getX();
            sumY += pointList.get(i).getY();
            // заполняем точки рисования
            renderPoints[i * 2 + 1] = pointList.get(i);
            renderPoints[i * 2 + 2] = pointList.get(i + 1);
            // заполняем цвета рисования
            renderColors[i * 2 + 1] = colorList.get(i);
            renderColors[i * 2 + 2] = colorList.get(i + 1);
        }

        // прибавляет координаты последней точки к сумме
        sumX += pointList.get(n - 1).getX();
        sumY += pointList.get(n - 1).getY();

        // заполняем точки рисования последнего треугольника
        renderPoints[n * 2 - 1] = pointList.get(n - 1);
        renderPoints[n * 2] = pointList.get(0);
        // заполняем цвета рисования последнего треугольника
        renderColors[n * 2 - 1] = colorList.get(n - 1);
        renderColors[n * 2] = colorList.get(0);

        // задаём общую опорную точку
        renderPoints[0] = new Point(sumX / n, sumY / n);
        renderColors[0] = Misc.getColor(180, 0, 255, 0);
    }

    /**
     * Нарисовать многоугольник
     *
     * @param canvas область рисования
     * @param p      перо
     */
    public void paint(Canvas canvas, Paint p) {
        canvas.drawTriangleFan(renderPoints, renderColors, p);
    }

    /**
     * Получить центр многоугольника
     *
     * @return центр многоугольника
     */
    public Point getCenter() {
        return renderPoints[0];
    }


}
