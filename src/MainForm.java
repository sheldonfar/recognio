import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

class MainForm extends JFrame {
    private JPanel rootPanel;
    private JButton origButton;
    private JButton modifButton;
    private JButton analyzeButton;
    private JButton prevButton;
    private JButton nextButton;
    private JLabel origLabel;
    private JLabel modifLabel;
    private JLabel resultLabel;
    private JLabel paginationLabel;
    private JTable resultTable;

    private final FlowController controller;
    private ImageComparator.ComparisonResult cr;
    private int posIterator;
    private final DefaultTableModel dtm = new DefaultTableModel(0, 0);

    private void setText(JLabel label) {
        label.setText(cr.getEqual() ? "Images are similar! " : "Images are different! ");
    }

    private void setPaginationText(int page) {
        paginationLabel.setText(++page + " / " + controller.totalResults());
    }

    private void fillTable(DefaultTableModel dtm) {
        ImageComparator.ComparisonResult.MethodResult orb = cr.getValueORB();
        ImageComparator.ComparisonResult.MethodResult surf = cr.getValueSURF();
        ImageComparator.ComparisonResult.MethodResult sift = cr.getValueSIFT();

        dtm.setRowCount(0);
        dtm.addRow(new Object[]{orb.methodName, orb.time, orb.value, orb.criterion, orb.decision});
        dtm.addRow(new Object[]{surf.methodName, surf.time, surf.value, surf.criterion, surf.decision});
        dtm.addRow(new Object[]{sift.methodName, sift.time, sift.value, sift.criterion, sift.decision});
    }

    private void resetView() {
        origLabel.setVisible(false);
        modifLabel.setVisible(false);
        prevButton.setVisible(false);
        nextButton.setVisible(false);
        resultLabel.setVisible(false);
        posIterator = 0;
    }

    private void analyze() {
        resetView();
        controller.analyze();

        cr = controller.getComparisonResult(0);
        fillTable(dtm);
        setText(resultLabel);

        BufferedImage orig = controller.getOriginalImage();
        BufferedImage modif = controller.getModifiedImage(0);
        origLabel.setIcon(new ImageIcon(orig));
        modifLabel.setIcon(new ImageIcon(modif));

        origLabel.setVisible(true);
        modifLabel.setVisible(true);
        resultLabel.setVisible(true);
        nextButton.setVisible(true);
        prevButton.setVisible(true);
        prevButton.setEnabled(false);
        nextButton.setEnabled(controller.totalResults() > 1);
        setPaginationText(0);

        setSize(new Dimension(orig.getWidth() + modif.getWidth() + 150, orig.getHeight() + modif.getHeight() + 100));
    }

    MainForm(FlowController controller) {
        this.controller = controller;
        setContentPane(rootPanel);
        JMenu fileMenu = new JMenu("File");
        JMenuItem saveItem = new JMenuItem("Save");
        fileMenu.add(saveItem);
        JMenuBar menuBar = new JMenuBar();
        menuBar.add(fileMenu);
        setJMenuBar(menuBar);
        pack();
        setSize(new Dimension(500, 350));
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setVisible(true);

        resetView();

        String[] COLUMN_NAMES = {"Method", "Time", "Value", "Criterion", "Decision"};
        dtm.setColumnIdentifiers(COLUMN_NAMES);
        resultTable.setModel(dtm);

        origButton.addActionListener(e -> {
            JFileChooser fc = new JFileChooser();
            fc.setCurrentDirectory(new File(System.getProperty("user.dir")));
            int returnVal = fc.showOpenDialog(rootPanel);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File file = fc.getSelectedFile();
                controller.setOriginalImage(file.getPath());
            }
        });

        modifButton.addActionListener(e -> {
            JFileChooser fc = new JFileChooser();
            fc.setMultiSelectionEnabled(true);
            fc.setCurrentDirectory(new File(System.getProperty("user.dir")));
            int returnVal = fc.showOpenDialog(rootPanel);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File[] files = fc.getSelectedFiles();
                controller.clearModifiedImages();
                for (File file : files) {
                    controller.addModifiedImage(file.getPath());
                }
            }
        });

        analyzeButton.addActionListener(e -> analyze());

        prevButton.addActionListener(e -> {
            if (posIterator - 1 >= 0) {
                cr = controller.getComparisonResult(--posIterator);
                fillTable(dtm);
                setText(resultLabel);
                modifLabel.setIcon(new ImageIcon(controller.getModifiedImage(posIterator)));
                prevButton.setEnabled(posIterator > 0);
                nextButton.setEnabled(true);
                setPaginationText(posIterator);
            }
        });

        nextButton.addActionListener(e -> {
            if (posIterator + 1 < controller.totalResults()) {
                cr = controller.getComparisonResult(++posIterator);
                fillTable(dtm);
                setText(resultLabel);
                modifLabel.setIcon(new ImageIcon(controller.getModifiedImage(posIterator)));
                nextButton.setEnabled(posIterator != controller.totalResults() - 1);
                prevButton.setEnabled(true);
                setPaginationText(posIterator);
            }
        });

        saveItem.addActionListener(e -> {
            JFileChooser fc = new JFileChooser();
            fc.setCurrentDirectory(new File(System.getProperty("user.dir")));

            int retVal = fc.showSaveDialog(null);
            if (retVal == JFileChooser.APPROVE_OPTION) {
                BufferedImage img = controller.getModifiedImage(posIterator);
                try {
                    File file = fc.getSelectedFile();
                    String[] tokens = file.getName().split("\\.(?=[^.]+$)");
                    if (tokens.length == 1) {
                        file = new File(file.toString() + ".png");
                    }
                    ImageIO.write(img, "png", file);
                } catch (IOException e1) {
                    System.out.println("Failed to save to file");
                }
            }
        });
    }
}
