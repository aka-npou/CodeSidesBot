import logic.*;
import model.*;

import java.util.*;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.lang.Math.*;
import static logic.Simulator.*;
import static model.Tile.*;
import static logic.Utils.*;
import static model.WeaponType.*;

public class MyStrategy {

    public static final ColorFloat BLACK = new ColorFloat(0, 0, 0, 1);
    public static final ColorFloat RED = new ColorFloat(1, 0, 0, 1);
    public static final ColorFloat GREEN = new ColorFloat(0, 1, 0, 1);
    public static final ColorFloat WHITE = new ColorFloat(1, 1, 1, 1);
    public static final double EXPLOSION_SIZE = 6;

    final boolean fake;
    final boolean bazookaOnly = false;

    Unit me;
    Game game;
    MyDebug debug;
    Tile[][] map;
    Simulator simulator = new Simulator();

    public MyStrategy() {
        fake = false;
    }

    public MyStrategy(boolean fake) {
        this.fake = fake;
    }

    public UnitAction getAction(Unit me, Game game, Debug debug0) {
        this.me = me;
        this.game = game;
        this.debug = fake ? new MyDebugStub() : new MyDebugImpl(debug0);
        this.map = game.getLevel().getTiles();

        //-------

        Unit enemy = chooseEnemy();
        LootBox targetBonus = chooseTargetBonus(enemy);
        MoveAction moveAction = move(enemy, targetBonus);
        Vec2Double aimDir = aim(enemy);
        boolean shoot = shouldShoot(enemy);

        boolean swap = bazookaOnly && me.getWeapon() != null && me.getWeapon().getTyp() != ROCKET_LAUNCHER;

        return new UnitAction(
                toApiSpeed(moveAction.speed),
                moveAction.jump,
                moveAction.jumpDown,
                aimDir,
                shoot,
                false,
                swap,
                false
        );
    }

    private void printMap() {
        System.out.println(Arrays.deepToString(map).replaceAll("\\[", "{").replaceAll("]", "}"));
    }

    List<MoveAction> moves = Stream.concat(
            Collections.nCopies(10, new MoveAction(0, true, false)).stream(),
            Collections.nCopies(10, new MoveAction(0, false, false)).stream()
    ).collect(Collectors.toList());

    double oldY;

    private UnitAction testSimulation() {
        UnitState state = new UnitState(me);
        System.out.println(state + ",");
        simulator.simulate(state, map, moves);
        MoveAction curAction = moves.get(game.getCurrentTick());
        oldY = me.getPosition().getY();
        return new UnitAction(
                toApiSpeed(curAction.speed),
                curAction.jump,
                curAction.jumpDown,
                new Vec2Double(0, 0),
                false,
                false,
                false,
                false
        );
    }

    private MoveAction move(Unit enemy, LootBox targetBonus) {
        MoveAction move = move0(enemy, targetBonus);
        /*if (!fake) {
            move = new MoveAction(0, false, false);
        } else {
            move = new MoveAction(move.speed, true, false);
        }/**/
        MoveAction dodge = tryDodgeBullets(move);
        if (dodge != null) {
            return dodge;
        }
        return move;
    }

    private MoveAction move0(Unit enemy, LootBox targetBonus) {
        Point targetPos = null;
        if (targetBonus != null && targetBonus.getItem() instanceof Item.HealthPack) {
            targetPos = heathPackTargetPoint(targetBonus);
        } else if (targetBonus != null) {
            targetPos = new Point(targetBonus.getPosition());
        } else if (!inLineOfSight(enemy)) {
            targetPos = new Point(enemy.getPosition());
        }
        if (targetPos == null) {
            return new MoveAction(0, false, false);
        } else {
            double myY = me.getPosition().getY();
            double myX = me.getPosition().getX();

            boolean jump = targetPos.y > myY;
            if ((int) targetPos.x > (int) myX && tileAtPoint(myX + 1, myY) == WALL) {
                jump = true;
            }
            if ((int) targetPos.x < (int) myX && tileAtPoint(myX - 1, myY) == WALL) {
                jump = true;
            }
            int platform = findPlatformAboveFloor();
            if (jump && platform != -1 && !enoughTimeToGetTo(platform)) {
                jump = false;
            }
            boolean jumpDown;
            if (jump) {
                jumpDown = false;
            } else {
                jumpDown = (int) targetPos.x == (int) myX && (int) targetPos.y < (int) myY;
            }
            return new MoveAction(getVelocity(targetPos), jump, jumpDown);
        }
    }

    private MoveAction tryDodgeBullets(MoveAction move) { // returns null if not in danger or can't dodge
        UnitState state = new UnitState(me);
        int steps = 100;
        List<UnitState> states = simulator.simulate(state, map, Collections.nCopies(steps, move));
        double dangerousDist = 0.5;
        double defaultDist = minDistToBulletOrExplosion(states);
        if (defaultDist > dangerousDist) {
            return null;
        }
        for (Bullet bullet : game.getBullets()) {
            List<Point> bulletPositions = simulator.simulateBullet(bullet, steps);
            for (Point p : bulletPositions) {
                debug.drawSquare(p, 0.1, RED);
            }
        }
        List<List<MoveAction>> plans = genPlans(steps);

        double maxDist = 0;
        List<MoveAction> bestPlan = null;
        for (List<MoveAction> plan : plans) {
            List<UnitState> dodgeStates = simulator.simulate(state, map, plan);
            double dist = minDistToBulletOrExplosion(dodgeStates);
            if (dist > maxDist) {
                maxDist = dist;
                bestPlan = plan;
            }
        }
        if (maxDist <= defaultDist) {
            return null;
        }
        return bestPlan.get(0);
    }

    private List<List<MoveAction>> genPlans(int steps) {
        List<List<MoveAction>> plans = new ArrayList<>();
        for (int standCnt = 0; standCnt <= steps; standCnt++) {
            plans.add(Stream.concat(
                    Collections.nCopies(standCnt, new MoveAction(0, false, false)).stream(),
                    Collections.nCopies(steps - standCnt, new MoveAction(0, true, false)).stream()
            ).collect(Collectors.toList()));
        }
        plans.add(Collections.nCopies(steps, new MoveAction(SPEED, false, false)));
        plans.add(Collections.nCopies(steps, new MoveAction(-SPEED, false, false)));
        return plans;
    }

    private double minDistToBulletOrExplosion(List<UnitState> states) {
        double minDist = Double.POSITIVE_INFINITY;
        for (Bullet bullet : game.getBullets()) {
            List<Point> bulletPositions = simulator.simulateBullet(bullet, states.size());
            for (int i = 0; i < bulletPositions.size(); i++) {
                Point bulletPos = bulletPositions.get(i);
                Point myPos = states.get(i).position;
                minDist = min(minDist, distToBullet(myPos, bulletPos, bullet.getSize()));
                if (bulletCollidesWithWall(map, bulletPos, bullet.getSize())) {
                    if (bullet.getExplosionParams() != null) {
                        minDist = min(
                                minDist,
                                distToBullet(myPos, bulletPos, bullet.getExplosionParams().getRadius() * 2)
                        );
                    }
                    break;
                }
            }
        }
        return minDist;
    }

    private static double distToBullet(Point myPos, Point bulletPos, double size) {
        double myX = myPos.x;
        double myY = myPos.y;
        return max(segmentDist(
                new Segment(myX - WIDTH / 2, myX + WIDTH / 2),
                new Segment(bulletPos.x - size / 2, bulletPos.x + size / 2)
        ), segmentDist(
                new Segment(myY, myY + HEIGHT),
                new Segment(bulletPos.y - size / 2, bulletPos.y + size / 2)
        ));
    }

    private static double segmentDist(Segment a, Segment b) {
        if (intersects(a, b)) {
            return 0;
        }
        return max(a.left - b.right, b.left - a.right);
    }

    private static boolean intersects(Segment a, Segment b) {
        return !(a.right < b.left || a.left > b.right);
    }

    static class Segment {
        final double left, right;

        Segment(double left, double right) {
            this.left = left;
            this.right = right;
        }
    }

    private Point heathPackTargetPoint(LootBox healthPack) {
        Point hpPos = new Point(healthPack.getPosition());
        if (me.getHealth() < 75) {
            return hpPos;
        }
        if ((int) me.getPosition().getY() != (int) healthPack.getPosition().getY()) {
            return hpPos;
        }
        double centerX = map.length / 2.0;
        double delta = healthPack.getSize().getX() / 2 + me.getSize().getX() / 2 + 0.1;
        if (hpPos.x < centerX) {
            return new Point(hpPos.x + delta, hpPos.y);
        } else {
            return new Point(hpPos.x - delta, hpPos.y);
        }
    }

    private boolean enoughTimeToGetTo(int platformFloor) {
        double curY = me.getPosition().getY();
        double remainingHeight = me.getJumpState().getSpeed() * me.getJumpState().getMaxTime();
        return (int) (curY + remainingHeight) >= platformFloor + 1;
    }

    private int findPlatformAboveFloor() {
        int x = (int) me.getPosition().getX();
        int y = (int) me.getPosition().getY();
        for (int j = y; j < map[0].length; j++) {
            if (map[x][j] == PLATFORM) {
                return j;
            }
        }
        return -1;
    }

    private boolean shouldShoot(Unit enemy) {
        Weapon weapon = me.getWeapon();
        if (weapon == null) {
            return false;
        }
        if (!inLineOfSight(enemy)) {
            return false;
        }
        if (canExplodeMyselfWithBazooka()) {
            return false;
        }
        return goodSpread(enemy, weapon);
    }

    private boolean canExplodeMyselfWithBazooka() {
        if (me.getWeapon().getTyp() != ROCKET_LAUNCHER) {
            return false;
        }
        debug.showSpread(me);
        double angle = me.getWeapon().getLastAngle();
        double spread = me.getWeapon().getSpread();
        return canExplodeMyselfWithBazooka(angle + spread) || canExplodeMyselfWithBazooka(angle - spread);
    }

    private boolean canExplodeMyselfWithBazooka(double angle) {
        Point bulletPos = muzzlePoint(me);
        BulletParams bullet = me.getWeapon().getParams().getBullet();
        double speed = fromApiSpeed(bullet.getSpeed());
        Point delta = Point.dir(angle).mult(speed);
        while (true) {
            bulletPos = bulletPos.add(delta);
            if (bulletCollidesWithWall(map, bulletPos, bullet.getSize())) {
                if (distToBullet(new Point(me), bulletPos, EXPLOSION_SIZE) <= 0.1) {
                    return true;
                } else {
                    break;
                }
            }
        }
        return false;
    }

    private boolean inLineOfSight(Unit enemy) {
        Point a = muzzlePoint(me);
        Point b = muzzlePoint(enemy);
        boolean blocked = false;
        int n = 1000;
        Point delta = b.minus(a).mult(1.0 / n);
        for (int i = 0; i < n; i++) {
            Point t = a.add(delta.mult(i));
            if (bulletCollidesWithWall(map, t, me.getWeapon().getParams().getBullet().getSize())) {
                blocked = true;
            }
        }
        return !blocked;
    }

    private Tile tileAtPoint(Point p) {
        return tileAtPoint(p.x, p.y);
    }

    private Tile tileAtPoint(double x, double y) {
        return Utils.tileAtPoint(map, x, y);
    }

    private boolean goodSpread(Unit enemy, Weapon weapon) { // todo rework
        if (true) {
            return true;
        }/**/
        double spread = weapon.getSpread();
        if (abs(spread - weapon.getParams().getMinSpread()) < 0.1) {
            return true;
        }
        double d = dist(me, enemy);
        double r = max(enemy.getSize().getX(), enemy.getSize().getY()) / 2;
        double angle = atan(r / d);
        return spread <= angle;
    }

    private static Point muzzlePoint(Unit unit) {
        return new Point(unit).add(new Point(0, unit.getSize().getY() / 2));
    }


    private double getVelocity(Point targetPos) {
        double r = targetPos.x - me.getPosition().getX();
        if (r > SPEED) {
            return SPEED;
        }
        if (r < -SPEED) {
            return -SPEED;
        }
        return r;
    }

    private Vec2Double aim(Unit enemy) {
        return new Vec2Double(
                enemy.getPosition().getX() - me.getPosition().getX(),
                enemy.getPosition().getY() - me.getPosition().getY()
        );
    }

    private LootBox chooseTargetBonus(Unit enemy) {
        Map<Class<? extends Item>, List<LootBox>> map = Stream.of(game.getLootBoxes())
                .collect(Collectors.groupingBy(b -> b.getItem().getClass()));
        List<LootBox> weapons = map.getOrDefault(Item.Weapon.class, Collections.emptyList());
        List<LootBox> healthPacks = map.getOrDefault(Item.HealthPack.class, Collections.emptyList());
        List<LootBox> mines = map.getOrDefault(Item.Mine.class, Collections.emptyList());

        if (me.getWeapon() == null || bazookaOnly && me.getWeapon().getTyp() != ROCKET_LAUNCHER) {
            return chooseWeapon(weapons);
        } else {
            return chooseHealthPack(healthPacks, enemy);
        }
    }

    private LootBox chooseHealthPack(List<LootBox> healthPacks, Unit enemy) {
        double centerX = map.length / 2.0;
        return healthPacks.stream()
                .min(
                        Comparator
                                .comparing((LootBox h) -> dist(h.getPosition(), me) > dist(h.getPosition(), enemy))
                                .thenComparing(h -> abs(h.getPosition().getX() - centerX))
                                .thenComparing(h -> dist(h.getPosition(), me.getPosition()))
                )
                .orElse(null);
    }

    private LootBox chooseWeapon(List<LootBox> weapons) {
        return weapons.stream()
                .filter(w -> !bazookaOnly || getType(w) == ROCKET_LAUNCHER)
                .min(Comparator.comparing(w -> dist(w.getPosition(), me.getPosition())))
                .orElse(null);
    }

    private WeaponType getType(LootBox lb) {
        return ((Item.Weapon) lb.getItem()).getWeaponType();
    }

    private Unit chooseEnemy() {
        Unit nearestEnemy = null;
        for (Unit other : game.getUnits()) {
            if (other.getPlayerId() != me.getPlayerId()) {
                if (nearestEnemy == null || sqrDist(me.getPosition(),
                        other.getPosition()) < sqrDist(me.getPosition(), nearestEnemy.getPosition())) {
                    nearestEnemy = other;
                }
            }
        }
        return nearestEnemy;
    }

    static double sqrDist(Vec2Double a, Vec2Double b) {
        return (a.getX() - b.getX()) * (a.getX() - b.getX()) + (a.getY() - b.getY()) * (a.getY() - b.getY());
    }

    interface MyDebug {
        void drawLine(Point a, Point b);

        void drawLine(Point a, Point b, ColorFloat color);

        void showSpread(Unit me);

        void drawSquare(Point p, double size, ColorFloat color);
    }

    static class MyDebugImpl implements MyDebug {
        final Debug debug;

        MyDebugImpl(Debug debug) {
            this.debug = debug;
        }

        private void drawLine(Unit a, Point b) {
            drawLine(new Point(a), b);
        }

        @Override
        public void drawLine(Point a, Point b) {
            drawLine(a, b, new ColorFloat(0f, 0f, 0f, 0.9f));
        }

        @Override
        public void drawLine(Point a, Point b, ColorFloat color) {
            debug.draw(new CustomData.Line(a.toV2F(), b.toV2F(), 0.1f, color));
        }

        @Override
        public void showSpread(Unit me) {
            Weapon weapon = me.getWeapon();
            if (weapon != null) {
                double curDir = weapon.getLastAngle();
                Point muzzle = muzzlePoint(me);

                showSpread(curDir, muzzle, weapon.getSpread());
                showSpread(curDir, muzzle, weapon.getParams().getMinSpread());
            }
        }

        private void showSpread(double curDir, Point muzzle, double spread) {
            Point to1 = muzzle.add(Point.dir(curDir + spread).mult(100));
            Point to2 = muzzle.add(Point.dir(curDir - spread).mult(100));

            drawLine(muzzle, to1);
            drawLine(muzzle, to2);
        }

        @Override
        public void drawSquare(Point p, double size, ColorFloat color) {
            Vec2Float pos = p.add(new Point(-size / 2, -size / 2)).toV2F();
            Vec2Float sizeV = new Vec2Float((float) size, (float) size);
            debug.draw(new CustomData.Rect(pos, sizeV, color));
        }
    }

    static class MyDebugStub implements MyDebug {
        @Override
        public void drawLine(Point a, Point b) {
        }

        @Override
        public void drawLine(Point a, Point b, ColorFloat color) {
        }

        @Override
        public void showSpread(Unit me) {
        }

        @Override
        public void drawSquare(Point p, double size, ColorFloat color) {
        }
    }

}