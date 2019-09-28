/*
 * Copyright (c) 2019. grgbanking all rights reserved.
 */

package com.tangqiang.monkey.polator;

import com.tangqiang.core.types.Point;

import java.util.Random;

/**
 * 圆弧,注意：点之间有抖动
 *
 * @author Tom
 * @version 1.0 2018-01-04 0004 Tom create
 * @date 2018-01-04 0004
 */
public class AngleInterpolator {
    private double angle;
    private Random random = new Random();

    public AngleInterpolator(double angle) {
        this.angle = angle;
    }


    public void interpolate(Point start, Point end, Callback callback) throws RuntimeException {
        if (start.getX() > end.getX() || start.getY() < end.getY()) {
            throw new RuntimeException("开始和结束点不符合要求！！！");
        }
        callback.start(start);
        double dRadius = circleRadius(start, end);
        double[] doubles = circleCenter(start, end, dRadius);
        double centerx = doubles[0];
        double centery = doubles[1];

        for (int i = start.getX(); i < end.getX(); ) {
            i = i + random.nextInt(8) + 3;
            double[] pointY = pointY(centerx, centery, dRadius, i);
            int randomY = random.nextInt(6);
            callback.step(new Point(Math.round(i), (int) Math.round(pointY[0] + randomY)));
        }

        callback.end(end);
    }

    private double circleRadius(Point p1, Point p2) {
        double diff = Math.sqrt((p1.getX() - p2.getX()) * (p1.getX() - p2.getX()) + (p1.getY() - p2.getY()) * (p1.getY() - p2.getY())) / 2;
        double radians = angle / 180 * Math.PI;
        return diff / Math.sin(radians);
    }

    private double[] pointY(double centerx, double centery, double dRadius, double x) {
        double tmp = Math.sqrt(dRadius * dRadius - (x - centerx) * (x - centerx));
        double y1 = centery - tmp;
        double y2 = centery + tmp;
        return new double[]{y1, y2};
    }

    private double[] circleCenter(Point p1, Point p2, double dRadius) {
        double center1x;
        double center2x;
        double center1y;
        double center2y;
        double k = (double) (p2.getY() - p1.getY()) / (double) (p2.getX() - p1.getX());
        // 如果
        if (k == 0) {
            center1x = (p1.getX() + p2.getX()) / 2.0;
            center2x = (p1.getX() + p2.getX()) / 2.0;
            center1y = p1.getY() + Math.sqrt(dRadius * dRadius - (p1.getX() - p2.getX()) * (p1.getX() - p2.getX()) / 4.0);
            center2y = p2.getY() - Math.sqrt(dRadius * dRadius - (p1.getX() - p2.getX()) * (p1.getX() - p2.getX()) / 4.0);
        } else {
            double kVerticle = -1.0 / k;
            double midX = (p1.getX() + p2.getX()) / 2.0;
            double midY = (p1.getY() + p2.getY()) / 2.0;
            double a = 1.0 + kVerticle * kVerticle;
            double b = -2 * midX - kVerticle * kVerticle * (p1.getX() + p2.getX());
            double c = midX * midX + kVerticle * kVerticle * (p1.getX() + p2.getX()) * (p1.getX() + p2.getX()) / 4.0 - (dRadius * dRadius - ((midX - p1.getX()) * (midX - p1.getX()) + (midY - p1.getY()) * (midY - p1.getY())));

            center1x = (-1.0 * b + Math.sqrt(b * b - 4 * a * c)) / (2 * a);
            center2x = (-1.0 * b - Math.sqrt(b * b - 4 * a * c)) / (2 * a);
            center1y = yCoordinates(midX, midY, kVerticle, center1x);
            center2y = yCoordinates(midX, midY, kVerticle, center2x);
        }
        return new double[]{center1x, center1y, center2x, center2y};
    }

    private double yCoordinates(double x, double y, double k, double x0) {
        return k * x0 - k * x + y;
    }


    public interface Callback {
        void start(Point var1);

        void end(Point var1);

        void step(Point var1);
    }
}

