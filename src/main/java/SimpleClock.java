import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.*;

public class SimpleClock {

    final static boolean isOSWindows = System.getProperty("os.name").toLowerCase().contains("win");
    final static String ANSI_CLS = "\u001b[2J";
    final static String ANSI_HOME = "\u001b[H";


    public static InputStream detectDigit() {
        if (isOSWindows)
            return SimpleClock.class.getResourceAsStream("digit_block");
        else
            return SimpleClock.class.getResourceAsStream("digit");
    }

    public static void setCursorHide(boolean setCursorHide) throws IOException, InterruptedException {
        if (!isOSWindows) {
            if (setCursorHide)
                new ProcessBuilder("printf", "'\\e[?25l'").inheritIO().start().waitFor();
            else
                new ProcessBuilder("printf", "'\\e[?25h'").inheritIO().start().waitFor();
        }


    }

    public static void clearTerminal() throws IOException, InterruptedException {
        if (isOSWindows) {
            new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
        } else {
            System.out.println(ANSI_CLS + ANSI_HOME);
        }
    }

    public static void printDate(String resultStringBuilder) throws IOException, InterruptedException {
        clearTerminal();
        if (isOSWindows)
            System.out.print(resultStringBuilder.replace("=", " "));
        else
            System.out.print(resultStringBuilder);
        System.out.flush();
    }

    public static void main(String[] args) {

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (!isOSWindows) {
                try {
                    setCursorHide(false);
                    clearTerminal();
                } catch (Exception ignored) {
                    System.exit(-1);
                }
            }
        }));

        ArrayList<ArrayList<String>> digitLists = new ArrayList<>();
        StringBuffer builtInDigitString = new StringBuffer();
        try {
            new BufferedReader(new InputStreamReader(Objects.requireNonNull(detectDigit()))).lines().forEach((digitLine) -> builtInDigitString.append(digitLine).append("\n"));
        } catch (Exception ignored) {
            System.err.println("E: There is no built-in digit file. Exit the program.");
            System.exit(-1);
        }
        Arrays.asList(builtInDigitString.deleteCharAt(builtInDigitString.length() - 1).toString().split("\n/\n")).forEach(digit -> digitLists.add(new ArrayList<>(Arrays.asList(digit.split("\n")))));

        try {
            long currentTimeStamp;

            Terminal terminal = TerminalBuilder.builder().build();
            setCursorHide(true);

            while (true) {
                try {
                    currentTimeStamp = System.currentTimeMillis();
                    String currentTime = new SimpleDateFormat("HH:mm:ss").format(new Date());

                    int digitWidth = digitLists.get(0).get(0).length();
                    int digitHeight = digitLists.get(0).size();

                    int col = terminal.getWidth(), line = terminal.getHeight();

                    int xStart = (int) Math.round(Math.floor((double) (col - (digitWidth * 8 + 7)) / 2));
                    int yStart = (int) Math.round(Math.floor((double) (line - digitHeight) / 2));

                    StringBuilder resultStringBuilder = new StringBuilder(String.join("", Collections.nCopies(yStart, "\n")));

                    for (int i = 0; i < digitHeight; i++) {
                        resultStringBuilder.append(String.join("", Collections.nCopies(xStart, " ")));
                        int digitIndex;
                        for (int j = 0; j < currentTime.length(); j++) {
                            digitIndex = ((digitIndex = Character.getNumericValue(currentTime.charAt(j))) < 0 ? 10 : digitIndex);
                            resultStringBuilder.append(digitLists.get(digitIndex).get(i)).append(" ");
                        }
                        resultStringBuilder.append("\n");
                    }
                    printDate(resultStringBuilder.toString());
                    while (true) {
                        if((System.currentTimeMillis() - currentTimeStamp) >= 1000)
                            break;
                    }
                } catch (Exception ignored) {
                }
            }
        } catch (IOException | InterruptedException ioException) {
            ioException.printStackTrace();
        }

//        String text = resultStringBuilder.toString();
//        String[] separatedTextArray = text.split("\n");
//        int textLine = separatedTextArray.length;
//
//        /*
//           Because font metrics is based on a graphics context, we need to create
//           a small, temporary image so we can ascertain the width and height
//           of the final image
//         */
//        BufferedImage img = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
//        Graphics2D g2d = img.createGraphics();
//
//        Font font =new Font("DejaVu Sans Mono", Font.PLAIN, 48);
//        g2d.setFont(font);
//        FontMetrics fm = g2d.getFontMetrics();
//
//        String maxStringLine = "";
//        for (String separatedText : separatedTextArray) {
//            if (separatedText.length() > maxStringLine.length())
//                maxStringLine = separatedText;
//        }
//
//        int width = fm.stringWidth(maxStringLine);
//        int height = fm.getHeight() * textLine;
//        g2d.dispose();
//
//        img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
//        g2d = img.createGraphics();
//        g2d.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
//        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
//        g2d.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
//        g2d.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_ENABLE);
//        g2d.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
//        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
//        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
//        g2d.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
//        g2d.setFont(font);
//
//        fm = g2d.getFontMetrics();
//        g2d.setColor(Color.BLACK);
//
//        for (int i = 1; i <= textLine; i++) {
//            g2d.drawString(separatedTextArray[i - 1], 0, fm.getAscent() * i);
//        }
//
//        g2d.dispose();
//        try {
//            ImageIO.write(img, "png", new File("Text.png"));
//        } catch (IOException ex) {
//            ex.printStackTrace();
//        }
    }
}