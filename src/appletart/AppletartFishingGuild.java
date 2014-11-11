package appletart;

import org.powerbot.script.*;
import org.powerbot.script.rt4.*;
import org.powerbot.script.rt4.ClientAccessor;
import org.powerbot.script.rt4.ClientContext;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.text.NumberFormat;

@Script.Manifest(name = "AppletartFishingGuild", description = "Fishes at the fishing guild!", properties = "client=4;")
public class AppletartFishingGuild extends PollingScript<ClientContext> implements PaintListener, MessageListener {

    private static final Area FISHING_SPOT = new Area(new Tile(2597, 3419), new Tile(2610, 3428));
    private static final Area BANK_AREA = new Area(new Tile(2585, 3413), new Tile(2595, 3423));

    public long startTime = 0;
    public long millis = 0;
    public long hours = 0;
    public long minutes = 0;
    public long seconds = 0;

    public int startLevel;
    public int lvlsGained;
    public int expGained = 0;
    public int startExp = 0;
    public int currLevel;
    private String status = "";

    private boolean sword_tuna = false;
    private String whatFishWCaps;
    private int poolID;
    private String whatAction;
    private String action_name;
    private int depositID;

    private boolean guiDone = false;

    @Override
    public void messaged(MessageEvent messageEvent) {
        if(sword_tuna){
            if(messageEvent.getMessage().contains(action_name) || messageEvent.getMessage().contains("a tuna")){
                fishCaught++;
            }
        }
        else {
            if (messageEvent.getMessage().contains(action_name)) {
                fishCaught++;
            }
        }
    }

    private enum State {
        BANK, FISH, IDLE, TO_BANK, TO_SPOT
    }

    private State state() {
        if(ctx.inventory.select().count() == 28 && BANK_AREA.contains(ctx.players.local())){
            return State.BANK;
        }
        if(ctx.inventory.select().count() == 28 && FISHING_SPOT.contains(ctx.players.local())){
            return State.TO_BANK;
        }
        if(ctx.inventory.select().count() != 28 && FISHING_SPOT.contains(ctx.players.local())){
            return State.FISH;
        }
        if(ctx.inventory.select().count() != 28 && BANK_AREA.contains(ctx.players.local())){
            return State.TO_SPOT;
        }
        return State.IDLE;
    }

    @Override
    public void start() {
        startExp = ctx.skills.experience(10);
        startLevel = ctx.skills.level(10);
        startTime = System.currentTimeMillis();
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new Gui(ctx);
            }
        });
    }

    @Override
    public void poll() {
        if(guiDone) {
            switch (state()) {
                case BANK:
                    status = "banking.";
                    if (ctx.bank.opened()) {
                        if (whatAction == "Sharks" && (ctx.inventory.select().id(371).poll().valid() || ctx.inventory.select().id(359).poll().valid())) {
                            ctx.bank.deposit(371, Bank.Amount.ALL);
                            ctx.bank.deposit(359, Bank.Amount.ALL);
                        }
                        if(sword_tuna){
                            ctx.bank.deposit(depositID, Bank.Amount.ALL);
                            ctx.bank.deposit(359, Bank.Amount.ALL);
                        }
                        else {
                            ctx.bank.deposit(depositID, Bank.Amount.ALL);
                        }
                        if(ctx.inventory.select().name("Big swordfish").poll().valid() || ctx.inventory.select().name("Big shark").poll().valid()){
                            ctx.bank.deposit(ctx.inventory.select().name("Big swordfish").poll().id(), Bank.Amount.ALL);
                            ctx.bank.deposit(ctx.inventory.select().name("Big shark").poll().id(), Bank.Amount.ALL);
                        }
                    } else {
                        GameObject bank = ctx.objects.select().id(11744).nearest().poll();
                        if (bank != null && bank.inViewport()) {
                            bank.interact("Bank");
                            sleep(400, 760);
                        }
                        else{
                            Random rn = new Random();
                            ctx.movement.findPath(new Tile(rn.nextInt(2588, 2590), rn.nextInt(3417, 3421))).traverse();
                        }
                    }
                    break;
                case FISH:
                    status = "at fishing spot.";
                    if (ctx.players.local().animation() == -1) {
                        Npc spot = ctx.npcs.select().within(FISHING_SPOT).id(poolID).nearest().viewable().poll();
                            if (spot != null) {
                                if(!spot.inViewport()){
                                    ctx.camera.turnTo(spot);
                                }
                                else {
                                    spot.interact(whatAction);
                                    sleep(1500, 2500);
                                }
                            }
                    } else {
                        if(ctx.players.local().interacting().valid() && ctx.players.local().interacting() instanceof Npc) {
                            final Npc npc = (Npc) ctx.players.local().interacting();
                            if(npc.id() != poolID || npc.id() == -1) {
                                Npc spot = ctx.npcs.select().within(FISHING_SPOT).id(poolID).nearest().poll();
                                if (spot != null && spot.inViewport()) {
                                    spot.interact(whatAction);
                                    sleep(1500, 2500);
                                }
                            }
                        }
                        Random rand = new Random();
                        int b = rand.nextInt(0, 85);
                        switch (b) {
                            case 1:
                            case 6:
                            case 19:
                                ctx.camera.pitch(rand.nextInt(50, 99));
                                break;
                            case 2:
                            case 44:
                            case 34:
                                ctx.camera.angle(rand.nextInt(1, 64));
                                break;
                            case 3:
                                Npc new_spot = ctx.npcs.select().within(FISHING_SPOT).id(1510).nearest().poll();
                                if (new_spot != null) {
                                    new_spot.click(false);
                                    ctx.input.move(rand.nextInt(0, 600), rand.nextInt(0, 400));
                                    int i = rand.nextInt(0, 5);
                                    if (i == 4) {
                                        ctx.input.click(false);
                                    }
                                    ctx.input.move(rand.nextInt(0, 50), rand.nextInt(0, 400));
                                    ctx.input.move(rand.nextInt(0, 600), rand.nextInt(0, 400));
                                }
                                break;
                            case 4:
                                ctx.input.move(rand.nextInt(0, 600), rand.nextInt(0, 400));
                                break;
                            case 14:
                                if (ctx.players.select().nearest() != null) {
                                    ctx.players.select().nearest().viewable().poll().click(false);
                                    sleep(2000, 5000);
                                    ctx.input.move(rand.nextInt(0, 600), rand.nextInt(0, 400));
                                    ctx.input.move(rand.nextInt(0, 600), rand.nextInt(0, 400));
                                    ctx.input.move(rand.nextInt(0, 600), rand.nextInt(0, 400));
                                }
                                break;
                            case 65:
                                if (ctx.npcs.select().nearest() != null) {
                                    ctx.npcs.select().nearest().viewable().poll().click(false);
                                    sleep(1000, 5000);
                                    ctx.input.move(rand.nextInt(0, 600), rand.nextInt(0, 400));
                                }
                                break;
                        }
                        sleep(500, 1000);

                    }
                    break;
                case TO_BANK:
                    status = "to bank.";
                    while (ctx.players.local().inMotion()) {
                        sleep(150, 250);
                    }
                    Random rn = new Random();
                    ctx.movement.findPath(new Tile(rn.nextInt(2588, 2590), rn.nextInt(3417, 3421))).traverse();
                    break;
                case TO_SPOT:
                    status = "to spot.";
                    while (ctx.players.local().inMotion()) {
                        sleep(150, 250);
                    }
                    ctx.movement.findPath(FISHING_SPOT.getRandomTile()).traverse();
                    break;
            }
        }
    }

    private final RenderingHints antialiasing = new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

    private final Color color1 = new Color(54, 176, 223, 45);
    private final Color color2 = new Color(0, 153, 255);
    private final Color color4 = new Color(255, 255, 153, 222);

    private final BasicStroke stroke1 = new BasicStroke(1);

    private final Font font1 = new Font("Arial", 0, 10);
    private int fishHour;
    private int expHour;
    private int fishCaught;

    final Image image = downloadImage("http://i.imgur.com/ZMlytoW.png");

    public void repaint(Graphics g1) {
        Graphics2D g = (Graphics2D)g1;
        g.setRenderingHints(antialiasing);
        millis = System.currentTimeMillis() - startTime;
        hours = millis / (1000 * 60 * 60);
        millis -= hours * (1000 * 60 * 60);
        minutes = millis / (1000 * 60);
        millis -= minutes * (1000 * 60);
        seconds = millis / 1000;
        fishHour = (int) ((fishCaught) * 3600000D / (System.currentTimeMillis() - startTime));
        expGained = ctx.skills.experience(10) - startExp;
        expHour = (int) ((expGained) * 3600000D / (System.currentTimeMillis() - startTime));
        currLevel = ctx.skills.level(10);
        lvlsGained = currLevel - startLevel;

        g.drawImage(image, 5, 233, null);
        g.setColor(color1);
        g.fillRect(5, 233, 129, 103);
        g.setColor(color2);
        g.setStroke(stroke1);
        g.drawRect(5, 233, 129, 103);
        g.setColor(color1);
        g.fillRect(137, 284, 377, 52);
        g.setColor(color2);
        g.drawRect(137, 284, 377, 52);
        g.setColor(color1);
        g.setFont(font1);
        g.setColor(color4);
        g.drawString("Time Ran: " + hours + ":" + minutes + ":" + seconds, 140, 299);
        g.drawString("Status: " + status, 140, 310);
        g.drawString("Current Level: " + currLevel + " (" + lvlsGained + ")", 140, 321);
        g.drawString("Version: 1.01", 140, 332);
        g.drawString("Fish Caught: " + NumberFormat.getInstance().format(fishCaught), 290, 299);
        g.drawString("Fish Hour: " + NumberFormat.getInstance().format(fishHour), 290, 310);
        g.drawString("Exp Gained: " + NumberFormat.getInstance().format(expGained), 290, 321);
        g.drawString("Exp Hour: " + NumberFormat.getInstance().format(expHour), 290, 332);
    }
    public void sleep(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void sleep(int ms1, int ms2) {
        int dif=ms2-ms1;
        sleep((int)((Math.random()*dif)+ms1));
    }
    class Gui extends ClientAccessor {
        private static final long serialVersionUID = 1L;

        JFrame frame = new JFrame("AppletartFishingGuild");

        public Gui(ClientContext ctx) {
            super(ctx);
            startGui();
            frame.setVisible(true);
        }

        public void startGui() {
            frame.setTitle("AppletartFishingGuild Select Fish");

            frame.setLayout(new BorderLayout());

            JPanel p = new JPanel();
            JPanel d = new JPanel();
            JLabel lab = new JLabel("Select fish:");
            JButton lobster = new JButton("Lobster");
            JButton sharks = new JButton("Sharks");
            JButton swordfishWithTuna = new JButton("Swordfish and keep Tuna");

            frame.add(lab, BorderLayout.NORTH);
            frame.add(p, BorderLayout.CENTER);
            frame.add(d, BorderLayout.SOUTH);

            p.setLayout(new FlowLayout());

            p.add(sharks);
            p.add(lobster);
            d.add(swordfishWithTuna);
            frame.pack();

            sharks.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    whatFishWCaps = "Sharks";
                    poolID = 1511;
                    whatAction = "Harpoon";
                    action_name = "a shark";
                    depositID = 383;
                    guiDone = true;
                    frame.setVisible(false);
                    frame.dispose();
                }
            });
            swordfishWithTuna.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    whatFishWCaps = "Swordfish and Tuna";
                    poolID = 1510;
                    whatAction = "Harpoon";
                    action_name = "a swordfish";
                    sword_tuna = true;
                    depositID = 371;
                    guiDone = true;
                    frame.setVisible(false);
                    frame.dispose();
                }
            });
            lobster.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    whatFishWCaps = "Lobsters";
                    poolID = 1510;
                    whatAction = "Cage";
                    action_name = "a lobster";
                    depositID = 377;
                    guiDone = true;
                    frame.setVisible(false);
                    frame.dispose();
                }
            });
        }
    }
}