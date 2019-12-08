import logic.*;
import model.*;

import java.util.*;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.lang.Math.*;
import static logic.Plan.*;
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
    public static final int HEALTHPACK_THRESHOLD = 75;

    final boolean fake;
    final boolean bazookaOnly = false;

    Unit me;
    Game game;
    MyDebug debug;
    Tile[][] map;
    Simulator simulator;

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
        if (game.getCurrentTick() == 0) {
            simulator = new Simulator(map);
        }

        //-------

        /*if (true) {
            return testSimulation();
        }/**/

        Unit enemy = chooseEnemy();
        LootBox targetBonus = chooseTargetBonus(enemy);
        MoveAction moveAction = move(enemy, targetBonus);
        Vec2Double aimDir = aim(enemy);
        boolean shoot = shouldShoot(enemy);

        boolean swap = me.getWeapon() != null && me.getWeapon().getTyp() == ROCKET_LAUNCHER;

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

    private UnitAction noop() {
        return new UnitAction(0, false, false, new Vec2Double(0, 0), false, false, false, false);
    }

    private void printMap() {
        System.out.println(Arrays.deepToString(map).replaceAll("\\[", "{").replaceAll("]", "}"));
    }

    Plan testPlan = plan(1, new MoveAction(0, false, false))
            .add(60, new MoveAction(0, true, false));

    private UnitAction testSimulation() {
        if (fake) {
            return noop();
        }
        if (game.getCurrentTick() == 0) {
            printMap();
        }
        UnitState state = new UnitState(me);
        System.out.println(state + ",");
        simulator.simulate(state, testPlan);
        MoveAction curAction = testPlan.get(game.getCurrentTick());
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
        Point targetPos;
        if (shouldGoToHealthPack(targetBonus)) {
            targetPos = heathPackTargetPoint(targetBonus);
        } else if (targetBonus != null && targetBonus.getItem() instanceof Item.Weapon) {
            targetPos = new Point(targetBonus.getPosition());
        } else {
            targetPos = findShootingPosition(enemy);
        }
        if (targetPos == null) {
            return new MoveAction(0, false, false);
        } else {
            debug.drawLine(new Point(me), targetPos, WHITE);
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

    private boolean shouldGoToHealthPack(LootBox targetBonus) {
        if (targetBonus == null || !(targetBonus.getItem() instanceof Item.HealthPack)) {
            return false;
        }
        if (me.getHealth() < HEALTHPACK_THRESHOLD) {
            return true;
        }
        return getMyPlayer().getScore() > getEnemyPlayer().getScore();
    }

    Player getMyPlayer() {
        return Stream.of(game.getPlayers())
                .filter(p -> p.getId() == me.getPlayerId())
                .findAny().get();
    }

    Player getEnemyPlayer() {
        return Stream.of(game.getPlayers())
                .filter(p -> p.getId() != me.getPlayerId())
                .findAny().get();
    }

    private Point findShootingPosition(Unit enemy) {
        double maxDist = Double.NEGATIVE_INFINITY;
        Point bestPoint = null;
        double myX = me.getPosition().getX();
        double enemyX = enemy.getPosition().getX();
        for (int x = 1; x < map.length - 1; x++) {
            for (int y = 1; y < map[0].length - 1; y++) {
                if (canBeInTile(x, y)) {
                    Point muzzlePoint = new Point(x + 0.5, y + HEIGHT / 2);
                    if (inLineOfSight(muzzlePoint, map, me.getWeapon(), enemy)) {
                        double dist = dist(muzzlePoint, enemy);
                        boolean sameSide = abs(myX - enemyX) < 1 || (myX < enemyX) == (muzzlePoint.x < enemyX);
                        if (!sameSide) {
                            dist -= 1000;
                        }
                        if (dist > maxDist) {
                            maxDist = dist;
                            bestPoint = new Point(x + 0.5, y);
                        }
                    }
                }
            }
        }
        return bestPoint;
    }


    private boolean canBeInTile(int x, int y) {
        if (map[x][y] == WALL) {
            return false;
        }
        if (map[x][y] == LADDER) {
            return true;
        }
        if (map[x][y - 1] == WALL || map[x][y - 1] == PLATFORM || map[x][y - 1] == LADDER) {
            return true;
        }
        return false;
    }

    private MoveAction tryDodgeBullets(MoveAction move) { // returns null if not in danger or can't dodge
        UnitState state = new UnitState(me);
        int steps = 100;
        List<UnitState> states = simulator.simulate(state, plan(steps, move));
        double defaultDanger = dangerFactor(states);
        if (defaultDanger <= 0) {
            return null;
        }
        for (Bullet bullet : game.getBullets()) {
            if (bullet.getPlayerId() == me.getPlayerId()) {
                continue;
            }
            List<Point> bulletPositions = simulator.simulateBullet(bullet, steps);
            for (Point p : bulletPositions) {
                debug.drawSquare(p, bullet.getSize(), RED);
                if (bulletCollidesWithWall(map, p, bullet.getSize())) {
                    if (bullet.getExplosionParams() != null) {
                        debug.drawSquare(p, bullet.getExplosionParams().getRadius() * 2, new ColorFloat(1f, 0f, 0f, 0.5f));
                    }
                    break;
                }
            }
        }
        List<Plan> plans = genPlans(steps);

        double minDanger = Double.POSITIVE_INFINITY;
        Plan bestPlan = null;
        for (Plan plan : plans) {
            List<UnitState> dodgeStates = simulator.simulate(state, plan);
            double danger = dangerFactor(dodgeStates);
            if (danger < minDanger) {
                minDanger = danger;
                bestPlan = plan;
            }
        }
        if (minDanger >= defaultDanger) {
            return null;
        }
        /*for (UnitState st : simulator.simulate(state, bestPlan)) {
            debug.drawSquare(st.position, 0.1, GREEN);
        }/**/
        return bestPlan.get(0);
    }

    private List<Plan> genPlans(int steps) {
        List<Plan> plans = new ArrayList<>();
        for (int standCnt = 0; standCnt <= steps; standCnt += 2) {
            plans.add(
                    plan(standCnt, new MoveAction(0, false, false))
                            .add(steps - standCnt, new MoveAction(0, true, false))
            );
        }
        for (int upCnt = 0; upCnt <= steps; upCnt += 2) {
            plans.add(
                    plan(upCnt, new MoveAction(0, true, false))
                            .add(steps - upCnt, new MoveAction(0, false, true))
            );
        }
        for (double speed : new double[]{-SPEED, 0, SPEED}) {
            for (boolean jump : new boolean[]{false, true}) {
                for (boolean jumpDown : new boolean[]{false, true}) {
                    if (jump && jumpDown) {
                        continue;
                    }
                    plans.add(plan(steps, new MoveAction(speed, jump, jumpDown)));
                }
            }
        }
        return plans;
    }

    private double dangerFactor(List<UnitState> states) {
        double minAllowedDist = 0.5;
        double danger = 0;
        for (Bullet bullet : game.getBullets()) {
            if (bullet.getPlayerId() == me.getPlayerId() && bullet.getWeaponType() != ROCKET_LAUNCHER) {
                continue;
            }
            List<Point> bulletPositions = simulator.simulateBullet(bullet, states.size());
            double minDist = Double.POSITIVE_INFINITY;

            ExplosionParams explosion = bullet.getExplosionParams();
            for (int i = 0; i < bulletPositions.size(); i++) {
                Point bulletPos = bulletPositions.get(i);
                Point myPos = states.get(i).position;
                double dist = distToBullet(myPos, bulletPos, bullet.getSize());
                minDist = min(minDist, dist);
                if (dist == 0) {
                    break;
                }
                if (bulletCollidesWithWall(map, bulletPos, bullet.getSize())) {
                    if (explosion != null) {
                        double distToExplosion = distToBullet(myPos, bulletPos, explosion.getRadius() * 2);
                        danger += getDanger(minAllowedDist, distToExplosion, explosion.getDamage());
                    }
                    break;
                }
            }

            int damage = bullet.getDamage();
            if (explosion != null) {
                damage += explosion.getDamage();
            }
            danger += getDanger(minAllowedDist, minDist, damage);
        }
        return danger;
    }

    private double getDanger(double minAllowedDist, double dist, int damage) {
        if (dist == 0) {
            return damage;
        }
        if (dist < minAllowedDist) {
            return minAllowedDist - dist;
        }
        return 0;
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
        if (me.getHealth() < HEALTHPACK_THRESHOLD) {
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
        return inLineOfSight(muzzlePoint(me), map, me.getWeapon(), enemy);
    }

    private static boolean inLineOfSight(Point a, Tile[][] map, Weapon weapon, Unit enemy) {
        Point b = muzzlePoint(enemy);
        boolean blocked = false;
        int n = 1000;
        Point delta = b.minus(a).mult(1.0 / n);
        for (int i = 0; i < n; i++) {
            Point t = a.add(delta.mult(i));
            if (bulletCollidesWithWall(map, t, weapon.getParams().getBullet().getSize())) {
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

        if (me.getWeapon() == null || me.getWeapon().getTyp() == ROCKET_LAUNCHER) {
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
                .filter(w -> getType(w) != ROCKET_LAUNCHER)
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