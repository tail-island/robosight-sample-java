package com.tail_island.robosight;

import net.arnx.jsonic.JSON;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Program {
  public static void main(String[] args) {
    new Program().execute();
  }

  private final double EPS = Math.pow(10, -8);
  private Random random = new Random();

  public void execute() {
    new BufferedReader(new InputStreamReader(System.in)).lines().map(s -> JSON.decode(s, Tank[][].class)).map(tss -> actions(tss[0], tss[1])).map(JSON::encode).forEach(System.out::println);
  }

  private List<Action> actions(Tank[] friends, Tank[] enemies) {
    return Arrays.stream(friends).map(friend -> action(friend, friends, enemies)).collect(Collectors.toList());
  }

  private Action action(Tank friend, Tank[] friends, Tank[] enemies) {
    // HPがないと何もできないので、何もしません。
    if (friend.hp <= 0.0) {
      return null;
    }

    // 一番弱っている敵を探します。
    Tank targetEnemy = Arrays.stream(enemies).filter(t -> t.hp > 0.0).min((t1, t2) -> (int)(t1.hp - t2.hp)).get();

    // 砲撃できる場合は、砲撃します。
    if (friend.canShootAfter < 2) {
      // まずは向き変え。
      double targetEnemyAngle = angle(sub(targetEnemy.center, friend.center));
      if (Math.abs(targetEnemyAngle - friend.direction) > Math.PI * 5 / 180) {
        return new Action("turn-to", targetEnemyAngle);
      }

      // 砲撃。
      return new Action("shoot", 10.0);
    }

    // 現在の速度を計算します。
    double speed = length(friend.velocity);

    // System.errには自由に出力できます。System.outは使っちゃ駄目。
    System.err.println(String.format("%s: speed = %.2f", friend.name, speed));

    // 速度が遅い場合は、乱数回避機動。。。(*^^*)
    if (speed < 5.0) {
      if (random.nextDouble() < 0.2) {
        // 適当に回転。
        return new Action("turn-to", random.nextDouble() * Math.PI * 2);
      }

      // 適当に加速。
      return new Action("forward", random.nextDouble() * 0.5 + 0.5);
    }

    // まずは、速度がゼロになる方向に向き変え。
    double antiVelocityAngle = normalizeAngle(angle(friend.velocity) + Math.PI);
    if (Math.abs(antiVelocityAngle - friend.direction) > EPS) {
      return new Action("turn-to", antiVelocityAngle);
    }
    
    // 加速。
    return new Action("forward", 1.0);  // 加速が大きすぎる場合は、システムが勝手に1.0まで下げます。
  }

  private double[] sub(double[] vector1, double[] vector2) {
    return new double[] {vector1[0] - vector2[0], vector1[1] - vector2[1]};
  }

  private double length(double[] vector) {
    return Math.sqrt(Math.pow(vector[0], 2) + Math.pow(vector[1], 2));
  }

  private double angle(double[] vector) {
    return Math.atan2(vector[1], vector[0]);
  }

  private double normalizeAngle(double angle) {
    return angle(new double[] { Math.cos(angle), Math.sin(angle) });
  }
}

class Tank {
  public double[] center;
  public double direction;
  public double[] velocity;
  public double hp;
  public int canShootAfter;
  public String name;
}

class Action {
  public String function;
  public double parameter;

  public Action(String function, double parameter) {
    this.function  = function;
    this.parameter = parameter;
  }
}
