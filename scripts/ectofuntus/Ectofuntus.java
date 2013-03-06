import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.IOException;
import java.net.URL;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.powerbot.core.event.listeners.PaintListener;
import org.powerbot.core.script.ActiveScript;
import org.powerbot.core.script.job.Task;
import org.powerbot.core.script.job.state.Node;
import org.powerbot.game.api.Manifest;
import org.powerbot.game.api.methods.Calculations;
import org.powerbot.game.api.methods.Game;
import org.powerbot.game.api.methods.Settings;
import org.powerbot.game.api.methods.Tabs;
import org.powerbot.game.api.methods.Walking;
import org.powerbot.game.api.methods.Widgets;
import org.powerbot.game.api.methods.input.Keyboard;
import org.powerbot.game.api.methods.input.Mouse;
import org.powerbot.game.api.methods.interactive.NPCs;
import org.powerbot.game.api.methods.interactive.Players;
import org.powerbot.game.api.methods.node.SceneEntities;
import org.powerbot.game.api.methods.tab.Equipment;
import org.powerbot.game.api.methods.tab.Inventory;
import org.powerbot.game.api.methods.tab.Skills;
import org.powerbot.game.api.methods.widget.Bank;
import org.powerbot.game.api.methods.widget.Camera;
import org.powerbot.game.api.util.Random;
import org.powerbot.game.api.util.Timer;
import org.powerbot.game.api.wrappers.Area;
import org.powerbot.game.api.wrappers.Tile;
import org.powerbot.game.api.wrappers.interactive.NPC;
import org.powerbot.game.api.wrappers.node.Item;
import org.powerbot.game.api.wrappers.node.SceneObject;
import org.powerbot.game.api.wrappers.widget.WidgetChild;
import org.powerbot.game.bot.Context;
import org.powerbot.game.client.Client;

/**
 * Created with IntelliJ IDEA.
 * Author: Buccaneer
 */

@Manifest(	authors = { "Buccaneer" },
        name = "Fast Funtus",
        description = "Cheap prayer training at the Ectofuntus",
        version = 1.05,
        website = "http://www.powerbot.org/community/topic/779397-ectofuntus-fast-funtus-efficient-prayer-training/"
)

public class Ectofuntus extends ActiveScript implements PaintListener {

    EctoGUI gui;

    private Client client = Context.client();

    private Area Burgh_de_Rott = new Area(new Tile(3480, 3220, 0), new Tile(3510, 3180, 0));
    private Area Daemonheim = new Area(new Tile(3440, 3690, 0), new Tile(3460, 3730, 0));
    private Area TzHaar = new Area(new Tile(4596, 5059, 0), new Tile(4613, 5070, 0));
    private Area Ectofuntus[] = {
            new Area(new Tile(3652, 3512, 0), new Tile(3667, 3527, 0)),
            new Area(new Tile(3673, 9878, 1), new Tile(3688, 9900, 1)),
            new Area(new Tile(3671, 9876, 2), new Tile(3689, 9900, 2)),
            new Area(new Tile(3668, 9873, 3), new Tile(3692, 9903, 3)),
            new Area(new Tile(3683, 9887, 0), new Tile(3683, 9889, 0))
    };

    private boolean START = false;
    private boolean task_worship = true;
    private boolean task_fill = false;
    private boolean filling = false;
    private boolean ringCheck = false;
    private boolean tokenCheck = false;
    private boolean startCheck = false;

    private final Font font1 = new Font("Verdana", 0, 11);
    private final Image background = getImage("http://oi48.tinypic.com/2rfpl4k.jpg");
    private long TTL = 0;
    private double START_EXP = 0;
    private double CURRENT_EXP = 0;
    private double GAINED_EXP = 0;
    private int START_LVL = 0;
    private int CURRENT_LVL = 0;
    private int GAINED_LVL = 0;
    private int allSlime = 0;
    private int invSlime = 0;
    private int bankSlime = 0;
    private String version = "1.05";
    private String buckets = "0";
    private String bucketsPerHour = "0";
    private String experience = "0";
    private String experiencePerHour = "0";
    private String levels = "0";
    private String TTLstring = "0";
    private String STATUS;
    private String TASK;
    private Timer runTime;

    private static int TELEPORT = 0;
    private static int ROK = 1;
    private static int TOKKUL = 2;
    private static int DRAKAN = 3;
    private static int PRAYER = 5;
    private static int SETTING_FULL_TOKENS = 0;
    private static int DISCIPLE_ID = 1686;
    private static int SLIME_ID = 4286;
    private static int BUCKET_ID = 1925;
    private static int PHIAL_ID = 4251;
    private static int PHIAL_EMPTY_ID = 4252;
    private static int POOL_ID = 17119;
    private static int TRAPDOOR_CLOSED_ID = 5267;
    private static int TRAPDOOR_OPEN_ID = 5268;
    private static int SHORTCUT_ID = 9308;
    private static int STAIRS_ID = 5263;
    private static int ECTOFUNTUS_ID = 5282;
    private static int ASH_ID = 20264; //Infernal: 20268, Impious: 20264

    private Node[] array = new Node[]{new Start(), new Banking(), new Bank2Labour(), new Labour(), new Labour2Bank()};

    public void waitWhile(boolean condition, int seconds) {
        for (int i = 0; i < seconds*100; i++) {
            if (condition) {
                Task.sleep(10);
            }
            i++;
        }
    }

    private void depositAll(int ITEM_ID) {
        if (Inventory.getCount(ITEM_ID) > 0) {
            Item i = Inventory.getItem(ITEM_ID);
            if (i != null) {
                i.getWidgetChild().interact("Deposit-All");
                waitWhile(Inventory.getCount(ITEM_ID) > 0, 1);
            }
        }
    }

    private boolean isNearEntity(int x) {
        final SceneObject entity = SceneEntities.getNearest(x);
        return entity != null;
    }

    private boolean isNextTo(int x) {
        final SceneObject entity = SceneEntities.getNearest(x);
        return entity != null && Calculations.distanceTo(entity.getLocation()) < 4;
    }

    private boolean doneBanking() {
        if (task_fill) {
            return Inventory.getCount(BUCKET_ID) > 0;
        } else {
            return Inventory.getCount(SLIME_ID) > 0 && Inventory.getCount(ASH_ID) > 0;
        }
    }

    private boolean doneLabour() {
        if (task_fill) {
            return Inventory.getCount(BUCKET_ID) == 0;
        } else {
            return Inventory.getCount(ASH_ID) == 0 || Inventory.getCount(SLIME_ID) == 0;
        }
    }

    private boolean bankIsOpen() {
        WidgetChild widgetChild = Widgets.get(762, 47);
        return widgetChild != null && widgetChild.validate();
    }

    private boolean fullTokens() {
        if (!tokenCheck && Widgets.get(1184, 13) != null && Widgets.get(1184, 13).validate()) {
            SETTING_FULL_TOKENS = Settings.get(2715);
            tokenCheck = true;
            return true;
        } else {
            return Settings.get(2715) >= SETTING_FULL_TOKENS;
        }
    }

    private Image getImage(String url) {
        try {
            return ImageIO.read(new URL(url));
        } catch(IOException e) {
            return null;
        }
    }

    @Override
    public void onStart() {
        log.info("Thanks for using Buccaneer's Ectofuntus script");
        log.info("Please report any bugs in the thread on the forums");
        START_LVL = Skills.getRealLevel(PRAYER);
        START_EXP = Skills.getExperience(PRAYER);
        gui = new EctoGUI();
        gui.setVisible(true);
    }

    @Override
    public int loop() {
        if (Game.getClientState() != Game.INDEX_MAP_LOADED) {
            return 1000;
        }
        if (client != Context.client()) {
            Context.get().getEventManager().addListener(this);
            client = Context.client();
        }
        if (task_fill && Tabs.INVENTORY.isOpen() && !Bank.isOpen()) {
            invSlime = Inventory.getCount(SLIME_ID);
            allSlime = invSlime + bankSlime;
        }
        for (final Node node : array)
            if (node.activate()) {
                node.execute();
                return 0;
            }
        return Random.nextInt(200, 300);
    }

    public class Start extends Node {

        @Override
        public boolean activate() {
            return (START && !startCheck);
        }

        @Override
        public void execute() {
            log.info("Performing node \"Start\" ");
            runTime = new Timer(0);
            startCheck = true;
        }
    }

    public class Banking extends Node {

        @Override
        public boolean activate() {
            if (SceneEntities.getNearest(Bank.BANK_BOOTH_IDS) != null) {
                return SceneEntities.getNearest(Bank.BANK_BOOTH_IDS).isOnScreen() && !doneBanking() && START;
            } else if (NPCs.getNearest(Bank.BANK_NPC_IDS) != null) {
                return NPCs.getNearest(Bank.BANK_NPC_IDS).isOnScreen() && !doneBanking() && START;
            }
            return false;
        }

        @Override
        public void execute() {

            log.info("Performing node \"Banking\" ");
            STATUS = "Banking";

            if (!Tabs.INVENTORY.isOpen()) {
                Tabs.INVENTORY.open();
            }

            if (task_fill) {
                bankSlime = invSlime + bankSlime;
                invSlime = 0;
            }

            if (!bankIsOpen()) {
                final NPC banker = NPCs.getNearest(Bank.BANK_NPC_IDS);
                final SceneObject booth = SceneEntities.getNearest(Bank.BANK_BOOTH_IDS);
                if (banker != null) {
                    if (banker.isOnScreen()) {
                        banker.interact("Bank");
                        waitWhile(!Bank.isOpen(), 5);
                    }
                } else if (booth != null) {
                    if (booth.isOnScreen()) {
                        new Tile(3494, 3211, 0).interact("Bank");
                        waitWhile(!Bank.isOpen(), 5);
                    }
                }
            }

            if (bankIsOpen()) {
                if (task_fill && Bank.getItemCount(true, BUCKET_ID) < 27) {
                    log.info("Not enough empty buckets left, switching to worshiping.");
                    task_fill = false;
                    task_worship = true;
                }

                if (task_worship && Bank.getItemCount(true, SLIME_ID) < 12) {
                    log.info("Not enough buckets of slime left, switching to filling.");
                    task_fill = true;
                    task_worship = false;
                }

                if (task_fill) {
                    Task.sleep(50,100);
                    if (Inventory.getCount(SLIME_ID) > 0) {
                        depositAll(SLIME_ID);
                    }
                    if (Inventory.getCount(SLIME_ID) == 0 && Inventory.getCount(BUCKET_ID) == 0) {
                        Bank.withdraw(BUCKET_ID, 27);
                    }
                    if (Inventory.getCount(BUCKET_ID) > 0) {
                        Bank.close();
                        Task.sleep(100,150);
                    }
                }

                if (task_worship) {
                    Task.sleep(50,100);
                    if (Inventory.getCount(BUCKET_ID) > 0) {
                        depositAll(BUCKET_ID);
                    }
                    if (Inventory.getCount(SLIME_ID) > 13) {
                        depositAll(SLIME_ID);
                    }
                    if (Inventory.getCount(ASH_ID) > 13) {
                        depositAll(ASH_ID);
                    }
                    if (Inventory.getCount(BUCKET_ID) == 0 && Inventory.getCount(SLIME_ID) == 0) {
                        Bank.withdraw(SLIME_ID, 13);
                        Task.sleep(200,350);
                    }
                    if (Inventory.getCount(BUCKET_ID) == 0 && Inventory.getCount(ASH_ID) == 0) {
                        Bank.withdraw(ASH_ID, 13);
                        Task.sleep(200,350);
                    }
                    if (Inventory.getCount(SLIME_ID) > 0 && Inventory.getCount(ASH_ID) > 0) {
                        Bank.close();
                        Task.sleep(200,350);
                    }
                }
            }

        }
    }

    public class Bank2Labour extends Node {

        @Override
        public boolean activate() {
            if (task_fill) {
                return doneBanking() && !isNextTo(POOL_ID) && START;
            } else {
                return doneBanking() && !Ectofuntus[0].contains(Players.getLocal().getLocation()) && START;
            }
        }

        @Override
        public void execute() {

            log.info("Performing node \"Bank2Labour\" ");

            if (Bank.isOpen()) {
                Bank.close();
            }

            if (task_fill) {
                STATUS = "Going to pool";
            } else {
                STATUS = "Going to ectofuntus";
            }

            if (!Tabs.INVENTORY.isOpen()) {
                Tabs.INVENTORY.open();
            }

            if (    Players.getLocal().getPlane() == 0
                    && !isNearEntity(ECTOFUNTUS_ID)
                    && !isNearEntity(TRAPDOOR_CLOSED_ID)
                    && !isNearEntity(TRAPDOOR_OPEN_ID)) {
                if (Inventory.getCount(PHIAL_ID) == 1) {
                    Item i = Inventory.getItem(PHIAL_ID);
                    i.getWidgetChild().interact("Empty");
                    Task.sleep(1500);
                    while (Inventory.getCount(PHIAL_EMPTY_ID) > 0) {
                        Task.sleep(100,200);
                    }
                } /*else if (Inventory.getCount(PHIAL_ID) == 0 && Inventory.getCount(PHIAL_EMPTY_ID) == 0) {
      			  	log.info("Could not find ectophial, logging out");
      			   	Game.logout(false);
      			   	stop();
      		   	}*/
            }

            if (Inventory.getCount(PHIAL_EMPTY_ID) == 1) {
                Item i = Inventory.getItem(PHIAL_EMPTY_ID);
                if (i != null) {
                    i.getWidgetChild().interact("Use");
                }
                final SceneObject ectofuntus = SceneEntities.getNearest(ECTOFUNTUS_ID);
                if (ectofuntus != null) {
                    ectofuntus.interact("Use");
                }
            }

            if (task_fill) {

                if (
                        Ectofuntus[0].contains(Players.getLocal().getLocation())
                                && !isNextTo(TRAPDOOR_CLOSED_ID)
                                && !isNextTo(TRAPDOOR_OPEN_ID)
                        )
                {
                    Tile destination = new Tile(3654, 3519, 0);
                    Walking.walk(destination.randomize(-1, 1));
                    waitWhile(!isNextTo(TRAPDOOR_CLOSED_ID) && !isNextTo(TRAPDOOR_OPEN_ID), 5);
                    Camera.setPitch(86);
                }

                if (isNextTo(TRAPDOOR_CLOSED_ID) || isNextTo(TRAPDOOR_OPEN_ID)) {
                    final SceneObject closedTrapdoor = SceneEntities.getNearest(TRAPDOOR_CLOSED_ID);
                    final SceneObject openTrapdoor = SceneEntities.getNearest(TRAPDOOR_OPEN_ID);
                    if (closedTrapdoor != null) {
                        closedTrapdoor.interact("Open");
                        waitWhile(SceneEntities.getNearest(TRAPDOOR_CLOSED_ID) != null, 2);
                    }
                    if (openTrapdoor != null) {
                        openTrapdoor.interact("Climb-down");
                        waitWhile(Players.getLocal().getPlane() == 0, 2);
                    }
                    Task.sleep(400,600);
                }

                if (Ectofuntus[3].contains(Players.getLocal().getLocation())) {
                    final SceneObject shortcut = SceneEntities.getNearest(SHORTCUT_ID);
                    shortcut.interact("Jump-down");
                    waitWhile(Players.getLocal().getPlane() == 3, 4);
                    Task.sleep(100,150);
                }

                if (Ectofuntus[2].contains(Players.getLocal().getLocation())) {
                    final SceneObject stairs = SceneEntities.getNearest(STAIRS_ID);
                    stairs.interact("Climb-down");
                    waitWhile(Players.getLocal().getPlane() == 2, 2);
                }

                if (Ectofuntus[1].contains(Players.getLocal().getLocation())) {
                    Tile destination = new Tile(3687, 9888, 1);
                    Walking.walk(destination.randomize(-1, 1));
                    final SceneObject stairs = SceneEntities.getNearest(STAIRS_ID);
                    Task.sleep(100,300);
                    if (stairs != null) {
                        if (!stairs.isOnScreen()) {
                            Camera.turnTo(stairs, 20);
                            waitWhile(!stairs.isOnScreen(), 5);
                        }
                        if (stairs.isOnScreen()) {
                            stairs.interact("Climb-down");
                            Task.sleep(200,500);
                            Item i = Inventory.getItem(BUCKET_ID);
                            i.getWidgetChild().interact("Use");
                            int a = Random.nextInt(-10,10);
                            int b = Random.nextInt(-10,10);
                            int x = (int) Players.getLocal().getCentralPoint().getX();
                            int y = (int) Players.getLocal().getCentralPoint().getY();
                            Mouse.move(x + a, y + b);
                            waitWhile(Ectofuntus[1].contains(Players.getLocal().getLocation()), 10);
                            Task.sleep(150,300);
                        }
                    }
                }
            }
        }
    }

    public class Labour extends Node {

        @Override
        public boolean activate() {
            if (task_fill) {
                return isNearEntity(POOL_ID) && !doneLabour() && START;
            } else {
                return isNearEntity(ECTOFUNTUS_ID) && !doneLabour() && START;
            }
        }

        @Override
        public void execute() {

            log.info("Performing node \"Labour\" ");

            if (task_fill) {
                STATUS = "Filling buckets";
                int previous = Inventory.getCount(SLIME_ID);
                if (!filling) {
                    Task.sleep(100,200);
                } else {
                    Task.sleep(3000,3500);
                }
                if (Inventory.getCount(SLIME_ID) == previous) {
                    if (Inventory.getCount(BUCKET_ID) > 0 && Inventory.getCount(SLIME_ID) < 28) {
                        if (Inventory.getSelectedItem() == null) {
                            Item i = Inventory.getItem(BUCKET_ID);
                            if (i != null) {
                                i.getWidgetChild().interact("Use");
                            }
                        }
                        final Tile slime = new Tile(3682, 9888, 0);
                        if (slime.isOnScreen()) {
                            slime.interact("Use");
                            filling = true;
                        }
                    }
                }
            }

            if (task_worship) {
                if (fullTokens()) {
                    STATUS = "Gathering tokens";
                    Task.sleep(500,800);
                    final NPC disciple = NPCs.getNearest(DISCIPLE_ID);
                    if (disciple != null) {
                        if (!disciple.isOnScreen()) {
                            int x = disciple.getLocation().getX();
                            int y = disciple.getLocation().getY();
                            Tile destination = new Tile(x, y, 0);
                            Walking.walk(destination.randomize(-1, 1));
                            waitWhile(!disciple.isOnScreen(), 3);
                        }
                        if (disciple.isOnScreen()) {
                            disciple.interact("Collect");
                            Task.sleep(500, 800);
                            waitWhile(Players.getLocal().isMoving(), 5);
                        }
                    }
                }
                if (!fullTokens()) {
                    STATUS = "Worshiping";
                    final SceneObject ectofuntus = SceneEntities.getNearest(ECTOFUNTUS_ID);
                    if (ectofuntus != null) {
                        if (!ectofuntus.isOnScreen()) {
                            Camera.turnTo(ectofuntus);
                            Camera.setPitch(Random.nextInt(20,40));
                        }
                        if (ectofuntus.isOnScreen()) {
                            if (ectofuntus.getModel().contains(Mouse.getLocation())) {
                                Mouse.click(true);
                                Task.sleep(200,400);
                                int whatdo = Random.nextInt(0, 8);
                                switch(whatdo) {
                                    case 0: Mouse.move(
                                            Mouse.getX() + Random.nextInt(-8,8),
                                            Mouse.getY() + Random.nextInt(-8,8)
                                    );
                                    case 1: break;
                                    default: break;
                                }
                            } else {
                                ectofuntus.interact("Worship");
                                Task.sleep(300, 500);
                            }
                        }
                    }
                }
            }
        }
    }

    public class Labour2Bank extends Node {

        @Override
        public boolean activate() {
            NPC banker = NPCs.getNearest(Bank.BANK_NPC_IDS);
            SceneObject booth = SceneEntities.getNearest(Bank.BANK_BOOTH_IDS);
            if (banker == null || !banker.isOnScreen()) {
                return doneLabour() && START;
            } else if (booth == null || !booth.isOnScreen()) {
                return doneLabour() && START;
            }
            return false;
        }

        @Override
        public void execute() {

            log.info("Performing node \"Labour2Bank\" ");

            STATUS = "Going to bank";

            if (task_fill) {
                filling = false;
            }

            if (!Bank.isOpen()) {
                if (!ringCheck) {
                    if (!Tabs.EQUIPMENT.isOpen()) {
                        Tabs.EQUIPMENT.open();
                    }
                    if (Equipment.getItem(Equipment.Slot.RING).getId() == 15707) {
                        TELEPORT = ROK;
                        log.info("Ring of Kinship detected, using Daemonheim bank");
                    } else if (Equipment.getItem(Equipment.Slot.RING).getId() == 23643) {
                        TELEPORT = TOKKUL;
                        log.info("Tokkul-Zo detected, using TzHaar bank");
                    } else if (Equipment.getItem(Equipment.Slot.NECK).getId() == 21576) {
                        TELEPORT = DRAKAN;
                        log.info("Drakan's Medallion detected, using Burgh de Rott bank");
                    } else {
                        log.info("Couldn't detect teleportation method");
                    }
                    ringCheck = true;
                }
                if (    TELEPORT == ROK && !Daemonheim.contains(Players.getLocal().getLocation()) ||
                        TELEPORT == TOKKUL && !TzHaar.contains(Players.getLocal().getLocation()) ||
                        TELEPORT == DRAKAN && !Burgh_de_Rott.contains(Players.getLocal().getLocation())) {
                    if (!Tabs.EQUIPMENT.isOpen()) {
                        Tabs.EQUIPMENT.open();
                    }
                    if (Tabs.EQUIPMENT.isOpen()) {
                        if (TELEPORT == TOKKUL) {
                            Equipment.getItem(Equipment.Slot.RING).getWidgetChild().interact("Teleport");
                            waitWhile(!Widgets.get(1188, 13).validate(), 4);
                            Task.sleep(100, 200);
                            Keyboard.sendText("2", false);
                            Task.sleep(800, 1200);
                            Tabs.INVENTORY.open();
                            waitWhile(!TzHaar.contains(Players.getLocal().getLocation()), 5);
                        } else if (TELEPORT == ROK) {
                            Equipment.getItem(Equipment.Slot.RING).getWidgetChild().interact("Teleport to Daemonheim");
                            Task.sleep(1000, 2000);
                            Tabs.INVENTORY.open();
                            waitWhile(!Daemonheim.contains(Players.getLocal().getLocation()), 10);
                        } else if (TELEPORT == DRAKAN) {
                            Equipment.getItem(Equipment.Slot.NECK).getWidgetChild().interact("Teleport");
                            waitWhile(!Widgets.get(1188, 13).validate(), 4);
                            Task.sleep(100, 200);
                            Keyboard.sendText("2", false);
                            Task.sleep(800, 1200);
                            Tabs.INVENTORY.open();
                            waitWhile(!Burgh_de_Rott.contains(Players.getLocal().getLocation()), 10);
                        }
                    }
                } else if (TELEPORT == ROK) {
                    waitWhile(Players.getLocal().getAnimation() != -1, 5);
                    Tile destination = new Tile(3449, 3719, 0);
                    Walking.walk(destination.randomize(-1, 1));
                    waitWhile(!isNextTo(48613), 3);
                } else if (TELEPORT == DRAKAN) {
                    Tile destination = new Tile(3495, 3211, 0);
                    Walking.walk(destination.randomize(-1, 1));
                    waitWhile(!isNextTo(48613), 10);
                }
            }
        }
    }

    @Override
    public void onRepaint(Graphics g) {

        if (START && runTime != null) {

            TASK = task_fill ? "Filling buckets" : "Worshiping";

            if (task_fill) {
                if (filling) {
                    invSlime = Inventory.getCount(SLIME_ID);
                    allSlime = invSlime + bankSlime;
                }
                buckets = Integer.toString(allSlime);
                bucketsPerHour = String.format("%.0f", (double) (allSlime * 3600 * 1000) / runTime.getElapsed());
            }

            if (task_worship) {
                CURRENT_EXP = Skills.getExperience(PRAYER);
                GAINED_EXP = (CURRENT_EXP - START_EXP); //
                experience = String.format("%.0f", GAINED_EXP);
                experiencePerHour = String.format("%.0f", ((GAINED_EXP * 3600 * 1000) / runTime.getElapsed()));
                CURRENT_LVL = Skills.getRealLevel(PRAYER);
                GAINED_LVL = CURRENT_LVL - START_LVL;
                levels = Integer.toString(GAINED_LVL);
				if (GAINED_EXP > 0) {
					TTL = (long) (runTime.getElapsed() / 1000 * Skills.getExperienceToLevel(PRAYER, CURRENT_LVL+1)
					/ GAINED_EXP);
					long hours = TTL / 3600;
					long remainder = TTL % 3600;
					long minutes = remainder / 60;
					long seconds = remainder % 60;
					TTLstring = Long.toString(hours) + " : "
					+ Long.toString(minutes) + " : "
					+ Long.toString(seconds);
				}
            }

            if (Game.getClientState() != 3) {
                g.setFont(font1);
                g.setColor(Color.WHITE);
                g.drawImage(background, 0, 0, null);
                g.drawString("Task: " + TASK, 74, 20);
                g.drawString("Status: " + STATUS, 74, 34);
                g.drawString("Time running: " + runTime.toElapsedString(), 234, 20);
                g.drawString("Version: " + version, 234, 34);
                if (task_fill) {
                    g.drawString("Buckets filled: " + buckets, 390, 20);
                    g.drawString("Buckets / hour: " + bucketsPerHour, 390, 34);
                }
                if (task_worship) {
                    g.drawString("Exp gained: " + experience, 390, 20);
                    g.drawString("Exp / hour: " + experiencePerHour, 390, 34);
                    g.drawString("Levels gained: " + levels, 546, 20);
                    g.drawString("TTL: " + TTLstring, 546, 34);
                }
            }

            g.setColor(Color.GREEN);
            g.drawLine(Mouse.getX() - 5, Mouse.getY() - 5, Mouse.getX() + 5, Mouse.getY() + 5);
            g.drawLine(Mouse.getX() - 5, Mouse.getY() + 5, Mouse.getX() + 5, Mouse.getY() - 5);

            if (Mouse.isPressed()) {
                Graphics2D g2 = (Graphics2D)g;
                g2.setColor(Color.RED);
                g2.setStroke(new BasicStroke(2));
                g2.drawLine(Mouse.getX() - 5, Mouse.getY() - 5, Mouse.getX() + 5, Mouse.getY() + 5);
                g2.drawLine(Mouse.getX() - 5, Mouse.getY() + 5, Mouse.getX() + 5, Mouse.getY() - 5);
            }
        }
    }

    public class EctoGUI extends JFrame {

        private static final long serialVersionUID = 1L;

        public JLabel methodLabel;

        public final String[] methods = {
                "Worship ectofuntus", "Fill buckets"
        };

        public JComboBox<String> methodBox;
        public JPanel methodPanel;
        public JButton startButton;

        public EctoGUI(){
            super("GUI");
            setLayout(new BorderLayout());
            setSize(220,90);
            setResizable(false);
            setLocationRelativeTo(null);
            Image rsbotlogo = getImage("https://si0.twimg.com/profile_images/247593948/rsbot_icon_normal.png");
            setIconImage(rsbotlogo);
            setAlwaysOnTop(true);

            methodLabel = new JLabel("What to do:");
            methodLabel.setHorizontalAlignment(JLabel.CENTER);

            methodBox = new JComboBox<String>(methods);
            methodBox.addItemListener(
                    new ItemListener(){
                        public void itemStateChanged(ItemEvent e){
                            int i = methodBox.getSelectedIndex();
                            task_worship = i == 0;
                            task_fill = i == 1;
                        }
                    }
            );

            methodPanel = new JPanel();
            methodPanel.add(methodLabel);
            methodPanel.add(methodBox);

            startButton = new JButton("Start");
            startButton.addActionListener(
                    new ActionListener(){
                        public void actionPerformed(ActionEvent e){
                            START = true;
                            dispose();
                        }
                    }
            );
            add(methodPanel, BorderLayout.CENTER);
            add(startButton, BorderLayout.SOUTH);
        }
    }

}
