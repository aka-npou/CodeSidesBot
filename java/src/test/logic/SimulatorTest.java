package logic;

import model.Tile;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static model.Tile.*;
import static org.testng.Assert.*;

import static logic.Utils.*;

public class SimulatorTest {
    @Test
    void test() {
        Simulator simulator = new Simulator();
        UnitState start = new UnitState(new Point(2.5, 1.0), 0);
        Tile[][] map = new Tile[][]{{WALL, WALL, WALL, WALL, WALL, WALL, WALL, WALL, WALL, WALL, WALL, WALL, WALL, WALL, WALL, WALL, WALL, WALL, WALL, WALL, WALL, WALL, WALL, WALL, WALL, WALL, WALL, WALL, WALL, WALL}, {WALL, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, WALL}, {WALL, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, WALL}, {WALL, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, WALL, WALL, WALL, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, WALL}, {WALL, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, WALL, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, WALL}, {WALL, LADDER, LADDER, LADDER, LADDER, EMPTY, EMPTY, WALL, EMPTY, EMPTY, EMPTY, WALL, WALL, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, WALL}, {WALL, EMPTY, EMPTY, EMPTY, WALL, EMPTY, EMPTY, WALL, EMPTY, EMPTY, EMPTY, EMPTY, WALL, JUMP_PAD, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, WALL, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, WALL}, {WALL, EMPTY, EMPTY, EMPTY, WALL, EMPTY, EMPTY, PLATFORM, EMPTY, EMPTY, EMPTY, EMPTY, WALL, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, WALL, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, WALL}, {WALL, EMPTY, EMPTY, EMPTY, WALL, EMPTY, EMPTY, PLATFORM, EMPTY, EMPTY, EMPTY, EMPTY, WALL, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, WALL, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, WALL}, {WALL, EMPTY, EMPTY, EMPTY, WALL, LADDER, LADDER, LADDER, EMPTY, EMPTY, EMPTY, EMPTY, WALL, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, WALL, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, WALL}, {WALL, EMPTY, EMPTY, EMPTY, WALL, EMPTY, EMPTY, PLATFORM, EMPTY, EMPTY, EMPTY, EMPTY, WALL, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, WALL, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, WALL}, {WALL, EMPTY, EMPTY, EMPTY, WALL, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, WALL, EMPTY, EMPTY, EMPTY, EMPTY, WALL, WALL, WALL, WALL, WALL, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, WALL}, {WALL, EMPTY, EMPTY, EMPTY, WALL, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, WALL, WALL, WALL, EMPTY, EMPTY, EMPTY, EMPTY, WALL, EMPTY, EMPTY, EMPTY, WALL, LADDER, LADDER, LADDER, LADDER, EMPTY, EMPTY, EMPTY, WALL}, {WALL, JUMP_PAD, EMPTY, EMPTY, PLATFORM, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, WALL, EMPTY, EMPTY, EMPTY, WALL, EMPTY, EMPTY, EMPTY, WALL}, {WALL, EMPTY, EMPTY, EMPTY, PLATFORM, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, PLATFORM, EMPTY, EMPTY, EMPTY, WALL, EMPTY, EMPTY, EMPTY, WALL}, {WALL, EMPTY, EMPTY, EMPTY, PLATFORM, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, PLATFORM, EMPTY, EMPTY, EMPTY, WALL, EMPTY, EMPTY, EMPTY, WALL}, {WALL, EMPTY, EMPTY, EMPTY, PLATFORM, EMPTY, EMPTY, EMPTY, WALL, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, PLATFORM, EMPTY, EMPTY, EMPTY, WALL, EMPTY, EMPTY, EMPTY, WALL}, {WALL, WALL, EMPTY, EMPTY, WALL, EMPTY, EMPTY, EMPTY, WALL, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, PLATFORM, EMPTY, EMPTY, EMPTY, WALL, EMPTY, EMPTY, EMPTY, WALL}, {WALL, EMPTY, EMPTY, EMPTY, PLATFORM, EMPTY, EMPTY, EMPTY, WALL, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, PLATFORM, EMPTY, EMPTY, EMPTY, EMPTY, PLATFORM, EMPTY, EMPTY, EMPTY, WALL, WALL, EMPTY, WALL, WALL}, {WALL, WALL, EMPTY, EMPTY, PLATFORM, EMPTY, EMPTY, EMPTY, WALL, JUMP_PAD, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, PLATFORM, EMPTY, EMPTY, EMPTY, EMPTY, PLATFORM, EMPTY, EMPTY, EMPTY, WALL, EMPTY, EMPTY, EMPTY, WALL}, {WALL, EMPTY, EMPTY, EMPTY, PLATFORM, EMPTY, EMPTY, EMPTY, WALL, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, WALL, WALL, WALL, EMPTY, EMPTY, EMPTY, EMPTY, PLATFORM, EMPTY, EMPTY, EMPTY, WALL, EMPTY, EMPTY, EMPTY, WALL}, {WALL, EMPTY, EMPTY, EMPTY, PLATFORM, EMPTY, EMPTY, EMPTY, WALL, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, PLATFORM, EMPTY, EMPTY, EMPTY, WALL, EMPTY, EMPTY, EMPTY, WALL}, {WALL, EMPTY, EMPTY, EMPTY, PLATFORM, EMPTY, EMPTY, EMPTY, WALL, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, PLATFORM, EMPTY, EMPTY, EMPTY, WALL, EMPTY, EMPTY, EMPTY, WALL}, {WALL, LADDER, LADDER, LADDER, PLATFORM, EMPTY, EMPTY, EMPTY, PLATFORM, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, PLATFORM, EMPTY, EMPTY, EMPTY, WALL, EMPTY, EMPTY, EMPTY, WALL}, {WALL, EMPTY, EMPTY, EMPTY, PLATFORM, EMPTY, EMPTY, EMPTY, PLATFORM, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, WALL, EMPTY, EMPTY, WALL, EMPTY, EMPTY, EMPTY, WALL, EMPTY, EMPTY, EMPTY, WALL}, {WALL, EMPTY, EMPTY, EMPTY, PLATFORM, EMPTY, EMPTY, EMPTY, PLATFORM, EMPTY, EMPTY, EMPTY, EMPTY, PLATFORM, EMPTY, EMPTY, EMPTY, EMPTY, WALL, WALL, WALL, WALL, EMPTY, EMPTY, EMPTY, WALL, EMPTY, EMPTY, EMPTY, WALL}, {WALL, EMPTY, EMPTY, EMPTY, WALL, EMPTY, EMPTY, EMPTY, PLATFORM, EMPTY, EMPTY, EMPTY, EMPTY, PLATFORM, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, WALL, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, WALL}, {WALL, EMPTY, EMPTY, EMPTY, WALL, EMPTY, EMPTY, EMPTY, PLATFORM, EMPTY, EMPTY, EMPTY, EMPTY, PLATFORM, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, WALL, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, WALL}, {WALL, EMPTY, EMPTY, EMPTY, WALL, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, WALL, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, WALL, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, WALL}, {WALL, EMPTY, EMPTY, EMPTY, WALL, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, WALL, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, WALL, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, WALL}, {WALL, EMPTY, EMPTY, EMPTY, WALL, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, WALL, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, WALL, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, WALL}, {WALL, EMPTY, EMPTY, EMPTY, WALL, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, LADDER, LADDER, LADDER, LADDER, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, WALL, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, WALL}, {WALL, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, WALL, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, WALL}, {WALL, JUMP_PAD, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, WALL, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, WALL}, {WALL, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, WALL, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, WALL}, {WALL, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, WALL}, {WALL, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, WALL}, {WALL, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, WALL}, {WALL, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, WALL}, {WALL, WALL, WALL, WALL, WALL, WALL, WALL, WALL, WALL, WALL, WALL, WALL, WALL, WALL, WALL, WALL, WALL, WALL, WALL, WALL, WALL, WALL, WALL, WALL, WALL, WALL, WALL, WALL, WALL, WALL}};
        List<UnitState> expected = Arrays.asList(
                new UnitState(new Point(2.5, 1.165000001000004), 0.5335000000000019),
                new UnitState(new Point(2.5, 1.3316666676666744), 0.516833333333337),
                new UnitState(new Point(2.5, 1.498333334333345), 0.5001666666666722),
                new UnitState(new Point(2.5, 1.6650000010000154), 0.48350000000000737),
                new UnitState(new Point(2.5, 1.831666667666686), 0.46683333333334254),
                new UnitState(new Point(2.5, 1.9983333343333565), 0.4501666666666777),
                new UnitState(new Point(2.5, 2.165000001000005), 0.4335000000000129),
                new UnitState(new Point(2.5, 2.3316666676666533), 0.41683333333334804),
                new UnitState(new Point(2.5, 2.4983333343333016), 0.4001666666666832),
                new UnitState(new Point(2.5, 2.66500000099995), 0.3835000000000184),
                new UnitState(new Point(2.5, 2.8316666676665982), 0.36683333333335355),
                new UnitState(new Point(2.5, 2.9983333343332466), 0.3501666666666887),
                new UnitState(new Point(2.5, 3.165000000999895), 0.3335000000000239),
                new UnitState(new Point(2.5, 3.331666667666543), 0.31683333333335906),
                new UnitState(new Point(2.5, 3.4983333343331915), 0.3001666666666942),
                new UnitState(new Point(2.5, 3.66500000099984), 0.2835000000000294),
                new UnitState(new Point(2.5, 3.831666667666488), 0.26683333333336456),
                new UnitState(new Point(2.5, 3.9983333343331364), 0.25016666666669973),
                new UnitState(new Point(2.5, 4.165000000999829), 0.23350000000003215),
                new UnitState(new Point(2.5, 4.331666667666521), 0.21683333333336455),
                new UnitState(new Point(2.5, 4.498333334333214), 0.20016666666669694),
                new UnitState(new Point(2.5, 4.665000000999907), 0.18350000000002933),
                new UnitState(new Point(2.5, 4.8316666676666), 0.16683333333336173),
                new UnitState(new Point(2.5, 4.998333334333292), 0.15016666666669412),
                new UnitState(new Point(2.5, 5.165000000999985), 0.13350000000002651),
                new UnitState(new Point(2.5, 5.331666667666678), 0.11683333333335959),
                new UnitState(new Point(2.5, 5.4983333343333705), 0.10016666666669337),
                new UnitState(new Point(2.5, 5.665000001000063), 0.08350000000002715),
                new UnitState(new Point(2.5, 5.831666667666756), 0.06683333333336093),
                new UnitState(new Point(2.5, 5.998333334333449), 0.0501666666666942),
                new UnitState(new Point(2.5, 6.165000001000141), 0.033500000000027286),
                new UnitState(new Point(2.5, 6.331666667666834), 0.016833333333360675),
                new UnitState(new Point(2.5, 6.498333334333527), 1.6666666669398568E-4),
                new UnitState(new Point(2.5, 6.338333334333502), 0.0),
                new UnitState(new Point(2.5, 6.171666667666809), 0.0),
                new UnitState(new Point(2.5, 6.005000001000116), 0.0),
                new UnitState(new Point(2.5, 5.838333334333424), 0.0),
                new UnitState(new Point(2.5, 5.671666667666731), 0.0),
                new UnitState(new Point(2.5, 5.505000001000038), 0.0),
                new UnitState(new Point(2.5, 5.3383333343333454), 0.0),
                new UnitState(new Point(2.5, 5.171666667666653), 0.0),
                new UnitState(new Point(2.5, 5.00500000099996), 0.0),
                new UnitState(new Point(2.5, 4.838333334333267), 0.0),
                new UnitState(new Point(2.5, 4.671666667666575), 0.0),
                new UnitState(new Point(2.5, 4.505000000999882), 0.0),
                new UnitState(new Point(2.5, 4.338333334333189), 0.0),
                new UnitState(new Point(2.5, 4.171666667666496), 0.0),
                new UnitState(new Point(2.5, 4.005000000999804), 0.0),
                new UnitState(new Point(2.5, 3.838333334333154), 0.0),
                new UnitState(new Point(2.5, 3.6716666676665057), 0.0),
                new UnitState(new Point(2.5, 3.5050000009998574), 0.0),
                new UnitState(new Point(2.5, 3.338333334333209), 0.0),
                new UnitState(new Point(2.5, 3.171666667666561), 0.0),
                new UnitState(new Point(2.5, 3.0050000009999125), 0.0),
                new UnitState(new Point(2.5, 2.838333334333264), 0.0),
                new UnitState(new Point(2.5, 2.671666667666616), 0.0),
                new UnitState(new Point(2.5, 2.5050000009999676), 0.0),
                new UnitState(new Point(2.5, 2.3383333343333192), 0.0),
                new UnitState(new Point(2.5, 2.171666667666671), 0.0),
                new UnitState(new Point(2.5, 2.0050000010000226), 0.0),
                new UnitState(new Point(2.5, 1.8383333343333528), 0.0),
                new UnitState(new Point(2.5, 1.6716666676666823), 0.0),
                new UnitState(new Point(2.5, 1.5050000010000117), 0.0),
                new UnitState(new Point(2.5, 1.3383333343333412), 0.0),
                new UnitState(new Point(2.5, 1.1716666676666707), 0.0),
                new UnitState(new Point(2.5, 1.0050000010000002), 0.0)
        );
        List<MoveAction> moves = Collections.nCopies(66, new MoveAction(0, true, false));
        List<UnitState> actual = simulator.simulate(start, map, moves);

        assertEquals(actual.size(), expected.size());
        for (int i = 0; i < actual.size(); i++) {
            UnitState act = actual.get(i);
            UnitState exp = expected.get(i);

            assertEquals(act.remainingJumpTime, exp.remainingJumpTime, 1e-8, "\ntick: " + (i + 1));
            assertTrue(dist(act.position, exp.position) <= 1e-8, "\ntick: " + (i + 1) + "\n" + act + "\n" + exp + "\n");
        }
    }
}