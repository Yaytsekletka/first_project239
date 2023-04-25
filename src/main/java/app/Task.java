package app;

import Misc.Vector2d;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.github.humbleui.jwm.MouseButton;
import io.github.humbleui.skija.*;
import Misc.CoordinateSystem2d;
import Misc.CoordinateSystem2i;
import Misc.Vector2i;
import lombok.Getter;
import panels.PanelLog;
import Misc.Misc;

import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

import static app.Colors.*;
import static app.Point.PointSet.FIRST_SET;

/**
 * Класс задачи
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, property = "@class")
public class Task {
    /**
     * Текст задачи
     */
    public static final String TASK_TEXT = """
           ПОСТАНОВКА ЗАДАЧИ:
           На плоскости задано множество "широких лучей" и множество 
           окружностей. Найти такую пару "широкий луч"-окружность, что
           фигура, находящаяся внутри"широкого луча" и окружности,
           имеет максимальную площадь. """;

    /**
     * Вещественная система координат задачи
     */
    @Getter
    private final CoordinateSystem2d ownCS;
    /**
     * Список окружностей
     */
    @Getter
    private final ArrayList<Circle> circles;
    /**
     * Список лучей
     */
    @Getter
    private final ArrayList<Ray> rays;
    /**
     * Список точек
     */
    @Getter
    private final ArrayList<Point> points;
    /**
     * Полигон
     */
    private final polygon poly;
    /**
     * Размер точки
     */
    private static final int POINT_SIZE = 2;
    /**
     * последняя СК окна
     */
    protected CoordinateSystem2i lastWindowCS;
    /**
     * Флаг, решена ли задача
     */
    private boolean solved;
    /**
     * Порядок разделителя сетки, т.е. раз в сколько отсечек
     * будет нарисована увеличенная
     */
    private static final int DELIMITER_ORDER = 10;
    /**
     * коэффициент колёсика мыши
     */
    private static final float WHEEL_SENSITIVE = 0.001f;



    /**
     * Задача
     *
     * @param ownCS  СК задачи
     * @param circles массив точек
     */
    @JsonCreator
    public Task(
            @JsonProperty("ownCS") CoordinateSystem2d ownCS,
            @JsonProperty("circles") ArrayList<Circle> circles,
            @JsonProperty("circles") ArrayList<Ray> rays,
             ArrayList<Point> points,
            polygon poly

    ) {
        this.ownCS = ownCS;
        this.circles = circles;
        this.rays = rays;
        this.points=points;
        this.poly = poly;
    }

    /**
     * Рисование
     *
     * @param canvas   область рисования
     * @param windowCS СК окна
     */
    public void paint(Canvas canvas, CoordinateSystem2i windowCS) {
        // Сохраняем последнюю СК
        lastWindowCS = windowCS;
        // рисуем координатную сетку
        renderGrid(canvas, lastWindowCS);
        // рисуем задачу
        renderTask(canvas, windowCS);
    }
    /**
     * Составление массива отрезков для рисования окружности в координатах окна
     * У движка есть готовый метод рисования набора отрезков canvas.drawLines(). Этому методу передать массив вещественных чисел float размером в четыре раза большим, чем кол-во линий. В этом массиве все данные идут подряд: сначала x координата первой точки, потом y координата, потом x координата второй точки, потом y координата, следующие четыре элемента точно также описывают второй отрезок и т.д.
     *
     * @param centre центр окружности
     * @param rad    радиус
     * @return набор точек окружности
     */
    public float[] arrCircle(Vector2d centre, double rad) {
        // радиус вдоль оси x
        float radX = (float) (rad);
        // радиус вдоль оси y
        float radY = (float) (rad);
        // кол-во отсчётов цикла
        int loopCnt = 100;
        // создаём массив координат опорных точек
        float[] points = new float[loopCnt * 4];
        // запускаем цикл
        for (int i = 0; i < loopCnt; i++) {
            // координаты первой точки в СК окна
            double tmpXold = centre.x + radX * Math.cos(2 * Math.PI / loopCnt * i);
            double tmpYold = centre.y + radY * Math.sin(2 * Math.PI / loopCnt * i);
            Vector2i tmp = lastWindowCS.getCoords(tmpXold, tmpYold, ownCS);
            // записываем x
            points[i * 4] = (float) tmp.x;
            // записываем y
            points[i * 4 + 1] = lastWindowCS.getMax().y - (float) tmp.y;
            // координаты второй точки в СК окна
            tmp = lastWindowCS.getCoords(centre.x + radX * Math.cos(2 * Math.PI / loopCnt * (i + 1)), centre.y + radY * Math.sin(2 * Math.PI / loopCnt * (i + 1)), ownCS);
            // записываем x
            points[i * 4 + 2] = (float) tmp.x;
            // записываем y
            points[i * 4 + 3] = lastWindowCS.getMax().y - (float) tmp.y;
        }
        return points;
    }
    /**
     * Рисование луча
     *
     * @param pos1 положение первой точки
     * @param pos2 положение второй точки
     */
    public void paintRay(Canvas canvas,Vector2i pos1, Vector2i pos2, Paint p, int maxDist) {
        // отрезок AB
        Vector2i AB = Vector2i.subtract(pos1, pos2);

        // создаём вектор направления для рисования условно бесконечной полосы
        Vector2d dir = new Vector2d(AB);
        //System.out.print("fx:"+dir.x+" fy:"+dir.y);
        dir = dir.rotated(Math.PI/2).norm();
        dir.mult(maxDist);
        Vector2i direction = new Vector2i((int)dir.x,(int)dir.y);
        // получаем точки рисования
        //System.out.println(" x:"+dir.x+" y:"+dir.y);
        Vector2i renderPointC = Vector2i.sum(pos1, direction);
        Vector2i renderPointD = Vector2i.sum(pos2, direction);

        // рисуем отрезки
        float oldW = p.getStrokeWidth();
        p.setStrokeWidth(3);
        canvas.drawLine(pos1.x, pos1.y, pos2.x, pos2.y, p);
        canvas.drawLine(pos1.x, pos1.y, renderPointC.x, renderPointC.y, p);
        canvas.drawLine(pos2.x, pos2.y, renderPointD.x, renderPointD.y, p);
        p.setStrokeWidth(oldW);
        // сохраняем цвет рисования
        int paintColor = p.getColor();
        // задаём красный цвет
        p.setColor(Misc.getColor(200, 255, 0, 0));
        // рисуем исходные точки
        canvas.drawRRect(RRect.makeXYWH(pos1.x - 3, pos1.y - 3, 6, 6, 3), p);
        canvas.drawRRect(RRect.makeXYWH(pos2.x - 3, pos2.y - 3, 6, 6, 3), p);
        // восстанавливаем исходный цвет рисования
        p.setColor(paintColor);


    }
    /**
     * Рисование задачи
     *
     * @param canvas   область рисования
     * @param windowCS СК окна
     */
    private void renderTask(Canvas canvas, CoordinateSystem2i windowCS) {
        canvas.save();
        // создаём перо
        try (var paint = new Paint()) {
            for(Circle c : circles){
                paint.setColor(CIRCLE_COLOR);
                // y-координату разворачиваем, потому что у СК окна ось y направлена вниз,
                // а в классическом представлении - вверх
                Vector2i windowPos = windowCS.getCoords(c.centre.x, c.centre.y, ownCS);
                // рисуем точку
                canvas.drawRRect( RRect.makeXYWH(windowPos.x - POINT_SIZE, lastWindowCS.getMax().y - (windowPos.y + POINT_SIZE), POINT_SIZE*2 , POINT_SIZE*2, POINT_SIZE ), paint);

                // рисуем окружность
                float[] points = arrCircle(c.centre, c.radius);
                canvas.drawLines(points, paint);
            }
            for(Ray r : rays){
                paint.setColor(RAY_COLOR);

                // а в классическом представлении - вверх
                Vector2i windowPos1 = windowCS.getCoords(r.pos1.x, r.pos1.y, ownCS);
                Vector2i windowPos2 = windowCS.getCoords(r.pos2.x, r.pos2.y, ownCS);
                windowPos1.y= lastWindowCS.getMax().y - windowPos1.y;
                windowPos2.y= lastWindowCS.getMax().y - windowPos2.y;

                // рисуем луч
                // получаем максимальную длину отрезка на экране, как длину диагонали экрана
                int maxDistance = (int) windowCS.getSize().length();
                paintRay(canvas,windowPos1,windowPos2,paint,maxDistance);
            }
            for (Point p : points) {
                // y-координату разворачиваем, потому что у СК окна ось y направлена вниз,
                // а в классическом представлении - вверх
                Vector2i windowPos = windowCS.getCoords(p.pos.x, p.pos.y, ownCS);
                // рисуем точку
                paint.setColor(INVISIBLE_COLOR);
                canvas.drawRRect(RRect.makeXYWH(windowPos.x - POINT_SIZE, windowPos.y - POINT_SIZE, POINT_SIZE * 2, POINT_SIZE * 2, POINT_SIZE), paint);
            }
            if(solved){
                poly.paint(canvas,paint);
            }

        }

        canvas.restore();
    }
    /**
     * Рисование сетки
     *
     * @param canvas   область рисования
     * @param windowCS СК окна
     */
    public void renderGrid(Canvas canvas, CoordinateSystem2i windowCS) {
        // сохраняем область рисования
        canvas.save();
        // получаем ширину штриха(т.е. по факту толщину линии)
        float strokeWidth = 0.03f / (float) ownCS.getSimilarity(windowCS).y + 0.5f;
        // создаём перо соответствующей толщины
        try (var paint = new Paint().setMode(PaintMode.STROKE).setStrokeWidth(strokeWidth).setColor(TASK_GRID_COLOR)) {
            // перебираем все целочисленные отсчёты нашей СК по оси X
            for (int i = (int) (ownCS.getMin().x); i <= (int) (ownCS.getMax().x); i++) {
                // находим положение этих штрихов на экране
                Vector2i windowPos = windowCS.getCoords(i, 0, ownCS);
                // каждый 10 штрих увеличенного размера
                float strokeHeight = i % DELIMITER_ORDER == 0 ? 5 : 2;
                // рисуем вертикальный штрих
                canvas.drawLine(windowPos.x, windowPos.y, windowPos.x, windowPos.y + strokeHeight, paint);
                canvas.drawLine(windowPos.x, windowPos.y, windowPos.x, windowPos.y - strokeHeight, paint);
            }
            // перебираем все целочисленные отсчёты нашей СК по оси Y
            for (int i = (int) (ownCS.getMin().y); i <= (int) (ownCS.getMax().y); i++) {
                // находим положение этих штрихов на экране
                Vector2i windowPos = windowCS.getCoords(0, i, ownCS);
                // каждый 10 штрих увеличенного размера
                float strokeHeight = i % 10 == 0 ? 5 : 2;
                // рисуем горизонтальный штрих
                canvas.drawLine(windowPos.x, windowPos.y, windowPos.x + strokeHeight, windowPos.y, paint);
                canvas.drawLine(windowPos.x, windowPos.y, windowPos.x - strokeHeight, windowPos.y, paint);
            }
        }
        // восстанавливаем область рисования
        canvas.restore();
    }


    /**
     * Рисование курсора мыши
     *
     * @param canvas   область рисования
     * @param windowCS СК окна
     * @param font     шрифт
     * @param pos      положение курсора мыши
     */
    public void paintMouse(Canvas canvas, CoordinateSystem2i windowCS, Font font, Vector2i pos) {
        // создаём перо
        try (var paint = new Paint().setColor(TASK_GRID_COLOR)) {
            // сохраняем область рисования
            canvas.save();
            // рисуем перекрестие
            canvas.drawRect(Rect.makeXYWH(0, pos.y - 1, windowCS.getSize().x, 2), paint);
            canvas.drawRect(Rect.makeXYWH(pos.x - 1, 0, 2, windowCS.getSize().y), paint);
            // смещаемся немного для красивого вывода текста
            canvas.translate(pos.x + 3, pos.y - 5);
            // положение курсора в пространстве задачи
            Vector2d realPos = getRealPos(pos.x, pos.y, lastWindowCS);
            // выводим координаты
            canvas.drawString(realPos.toString(), 0, 0, font, paint);
            // восстанавливаем область рисования
            canvas.restore();
        }
    }


    /**
     * Масштабирование области просмотра задачи
     *
     * @param delta  прокрутка колеса
     * @param center центр масштабирования
     */
    public void scale(float delta, Vector2i center) {
        if (lastWindowCS == null) return;
        // получаем координаты центра масштабирования в СК задачи
        Vector2d realCenter = ownCS.getCoords(center, lastWindowCS);
        // выполняем масштабирование
        ownCS.scale(1 - delta * WHEEL_SENSITIVE, realCenter);
    }


    /**
     * Получить положение курсора мыши в СК задачи
     *
     * @param x        координата X курсора
     * @param y        координата Y курсора
     * @param windowCS СК окна
     * @return вещественный вектор положения в СК задачи
     */
    @JsonIgnore
    public Vector2d getRealPos(int x, int y, CoordinateSystem2i windowCS) {
        return ownCS.getCoords(x, y, windowCS);
    }

    
    Vector2d prevClickPos = null;

    /**
     * Клик мыши по пространству задачи
     *
     * @param pos         положение мыши
     * @param mouseButton кнопка мыши
     */
    public void click(Vector2i pos, MouseButton mouseButton) {
        if (lastWindowCS == null) return;
        // переворачиваем y
        Vector2i pos1 = new Vector2i(pos.x, lastWindowCS.getMax().y - pos.y);
        // получаем положение на экране
        Vector2d taskPos = ownCS.getCoords(pos1, lastWindowCS);
        if (prevClickPos!=null && (taskPos.x!=prevClickPos.x||taskPos.y!=prevClickPos.y)) {
            // если левая кнопка мыши, добавляем в первое множество
            //double tmpR = ThreadLocalRandom.current().nextDouble(0, Math.min(ownCS.getSize().x, ownCS.getSize().y) / 2);
            if (mouseButton.equals(MouseButton.PRIMARY)) {
                // pos, prevClickPos
                // addPoint(taskPos, Point.PointSet.FIRST_SET);
                double tmpR = Math.sqrt((taskPos.x-prevClickPos.x)*(taskPos.x-prevClickPos.x)+(taskPos.y-prevClickPos.y)*(taskPos.y-prevClickPos.y));
                addCircle(prevClickPos, tmpR);
                prevClickPos=null;
                // если правая, то во второе
            } else if (mouseButton.equals(MouseButton.SECONDARY)) {
                // addPoint(taskPos, Point.PointSet.SECOND_SET);
                addRay(prevClickPos,taskPos);
                prevClickPos=null;
            }
        }else {
            prevClickPos = taskPos;
        }

    }
    /**
     * Добавить окружность
     *
     * @param center положение центра
     * @param radius радиус
     */
    public void addCircle(Vector2d center, double radius) {
        solved = false;
        Circle newCircle = new Circle(center, radius);
        circles.add(newCircle);
        PanelLog.info("окружность " + newCircle + " добавлена в задачу");
    }

    /**
     * Добавить случайные окружности
     *
     * @param cnt кол-во случайных окружностей
     */
    public void addRandomCircles(int cnt) {
        // повторяем заданное количество раз
        for (int i = 0; i < cnt; i++) {
            // получаем случайные координаты центра
            Vector2d pos = ownCS.getRandomCoords();
            //получаем случайный радиус
            double tmpR = ThreadLocalRandom.current().nextDouble(0, Math.min(ownCS.getSize().x, ownCS.getSize().y) / 2);
            addCircle(pos, tmpR);
        }
    }
    /**
     * Добавить Луч
     *
     * @param pos1 положение первой точки
     * @param pos2 положение второй точки
     */
    public void addRay(Vector2d pos1, Vector2d pos2) {
        solved = false;
        Ray newRay = new Ray(pos1,pos2 );
        rays.add(newRay);
        PanelLog.info("окружность " + newRay + " добавлена в задачу");
    }

    /**
     * Добавить случайные лучи
     *
     * @param cnt кол-во случайных лучей
     */
    public void addRandomRays(int cnt) {
        // повторяем заданное количество раз
        for (int i = 0; i < cnt; i++) {
            // получаем случайные координаты для двух точек
            Vector2d pos1 = ownCS.getRandomCoords();
            Vector2d pos2 = ownCS.getRandomCoords();
            addRay(pos1, pos2);
        }
    }
    /**
     * Добавить точку
     *
     * @param pos      положение
     */
    public void addPoint(Vector2d pos) {
        solved = false;
        Point newPoint = new Point(pos, FIRST_SET);
        points.add(newPoint);
        PanelLog.info("точка " + newPoint + " добавлена в " + newPoint.getSetName());
    }
    /**
     * Добавить случайные точки
     *
     * @param cnt кол-во случайных точек
     */
    public void addRandomPoints(int cnt) {
        CoordinateSystem2i addGrid = new CoordinateSystem2i(300, 300);
        for (int i = 0; i < cnt; i++) {
            Vector2i gridPos = addGrid.getRandomCoords();
            Vector2d pos = ownCS.getCoords(gridPos, addGrid);
            // сработает примерно в половине случаев
            if (ThreadLocalRandom.current().nextBoolean())
                addPoint(pos);
            else
                addPoint(pos);
        }
    }
    /**
     * Очистить задачу
     */
    public void clear() {
        //points.clear();
        rays.clear();
        circles.clear();
        solved = false;
    }
    public static boolean pointInRectangle(double x1, double y1, double x2, double y2, double x3, double y3, double x4, double y4, double x, double y) {
        // уравнения прямых, проходящих через каждую сторону прямоугольника
        double a1 = y2 - y1;
        double b1 = x1 - x2;
        double c1 = x2*y1 - x1*y2;

        double a2 = y3 - y2;
        double b2 = x2 - x3;
        double c2 = x3*y2 - x2*y3;

        double a3 = y4 - y3;
        double b3 = x3 - x4;
        double c3 = x4*y3 - x3*y4;

        double a4 = y1 - y4;
        double b4 = x4 - x1;
        double c4 = x1*y4 - x4*y1;

        // проверка принадлежности точки каждой стороне прямоугольника
        boolean side1 = a1*x + b1*y + c1 <= 0;
        boolean side2 = a2*x + b2*y + c2 <= 0;
        boolean side3 = a3*x + b3*y + c3 <= 0;
        boolean side4 = a4*x + b4*y + c4 <= 0;

        // точка находится внутри прямоугольника, если она находится с одной стороны от каждой стороны прямоугольника
        return side1 == side2 && side2 == side3 && side3 == side4;
    }
    /**
     * Решить задачу
     */
    public void solve() {
        // выделяем область в которой будем раскидывать точки
        addRandomPoints(5000);
        //int minPosX=0,minPosY=0,maxPosX=0,maxPosY=0;
        // minPos крайняя левая точка области
        //
//        for(Circle c : circles){
//            if(c.centre.x < (double) minPosX) minPosX=(int) c.centre.x +1;
//            if(c.centre.x > (double) maxPosX) maxPosX=(int) c.centre.x+1;
//            if(c.centre.y < (double) minPosY) minPosY=(int) c.centre.y+1;
//            if(c.centre.y > (double) maxPosY) maxPosY=(int) c.centre.y+1;
//        }
        int [][] myarr = new int[circles.size()][rays.size()];
        for (int i=0;i < points.size();i++) {
            Point p = points.get(i);
            for (int j=0;j < circles.size();j++) {
                Circle c = circles.get(j);
                for (int x=0;x < rays.size();x++) {
                    Ray r = rays.get(x);
                    boolean isInCircle=false;
                    boolean isInRay=false;
                    if(Math.sqrt((p.pos.x-c.centre.x)*(p.pos.x-c.centre.x) + (p.pos.y-c.centre.y)*(p.pos.y-c.centre.y))<c.radius){
                        isInCircle=true;
                    }
                    Vector2d pos1 = r.pos1;
                    Vector2d pos2 = r.pos2;
                    int maxDist = 100;
                    Vector2d dir = new Vector2d(r.pos2.x-r.pos1.x,r.pos2.y-r.pos1.y);
                    dir = dir.rotated(Math.PI/2).norm();
                    dir.mult(maxDist);
                    Vector2i direction = new Vector2i((int)dir.x,(int)dir.y);
                    Vector2d renderPointC = Vector2d.sum(r.pos1, dir);
                    Vector2d renderPointD = Vector2d.sum(r.pos2, dir);

                    isInRay = pointInRectangle(pos1.x,pos1.y,pos2.x,pos2.y,renderPointD.x,renderPointD.y,renderPointC.x,renderPointC.y,p.pos.x,p.pos.y);
                    if(isInRay)myarr[j][x]++;
                }
            }
        }
        int posCircleMax=0;
        int posRayMax=0;
        int max=0;
        for(int i = 0; i < circles.size(); i++){
            for(int j = 0; j < rays.size(); j++){
               if(max < myarr[i][j]){
                   posCircleMax=i;
                   posRayMax=j;
               }
            }
        }
        System.out.println("posRayMax:"+posRayMax);
        Circle ansCircle = circles.get(posCircleMax);
        Ray ansRay = rays.get(posRayMax);
        Vector2d dir = new Vector2d(ansRay.pos2.x-ansRay.pos1.x,ansRay.pos2.y-ansRay.pos1.y);
        dir = dir.rotated(Math.PI/2).norm();
        int maxDist=100;
        dir.mult(maxDist);

        Vector2d renderPointC = Vector2d.sum(ansRay.pos1, dir);
        Vector2d renderPointD = Vector2d.sum(ansRay.pos2, dir);
        Vector2i pos1 = lastWindowCS.getCoords(ansRay.pos1.x,ansRay.pos1.y, ownCS); //
        Vector2i pos2 = lastWindowCS.getCoords(ansRay.pos2.x,ansRay.pos2.y, ownCS);
        Vector2i pos3 = lastWindowCS.getCoords(renderPointC.x,renderPointC.y, ownCS);
        Vector2i pos4 = lastWindowCS.getCoords(renderPointD.x,renderPointD.y, ownCS);
        poly.add(pos1.x,lastWindowCS.getMax().y - pos1.y,Misc.getColor(100,200,200,0));
        poly.add(pos2.x,lastWindowCS.getMax().y - pos2.y,Misc.getColor(100,200,200,0));
        poly.add(pos3.x,lastWindowCS.getMax().y - pos3.y,Misc.getColor(100,200,200,0));
        poly.add(pos4.x,lastWindowCS.getMax().y - pos4.y,Misc.getColor(100,200,200,0));
        poly.calculate();
        // перебираем пары точек
//        for (int i = 0; i < points.size(); i++) {
//            for (int j = i + 1; j < points.size(); j++) {
//                // сохраняем точки
//                Point a = points.get(i);
//                Point b = points.get(j);
//                // если точки совпадают по положению
//                if (a.pos.equals(b.pos) && !a.pointSet.equals(b.pointSet)) {
//                    if (!crossed.contains(a)){
//                        crossed.add(a);
//                        crossed.add(b);
//                    }
//                }
//            }
//        }

        /// добавляем вс
//        for (Point point : points)
//            if (!crossed.contains(point))
//                single.add(point);

        // задача решена
        solved = true;
    }
    /**
     * Отмена решения задачи
     */
    public void cancel() {
         solved = false;
         points.clear();
         poly.clear();
    }
    /**
     * проверка, решена ли задача
     *
     * @return флаг
     */
    public boolean isSolved() {
        return solved;
    }
}
