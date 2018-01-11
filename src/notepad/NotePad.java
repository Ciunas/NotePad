package notepad;

import java.awt.EventQueue;
import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.JScrollPane;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import java.awt.Color;
import java.awt.Component;
import javax.swing.Box;
import javax.swing.JButton; 
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.text.BadLocationException; 
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeSelectionModel;
import java.awt.GridLayout;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths; 
import java.util.HashMap; 
import java.util.prefs.Preferences;
import net.miginfocom.swing.MigLayout;
import javax.swing.JLabel;
import java.awt.Dimension;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.ActionEvent;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JMenu;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import javax.swing.border.TitledBorder;
import javax.swing.border.LineBorder;
import javax.swing.ImageIcon;

/**
 * @author ciunas
 *
 */

public class NotePad {

	HashMap<String, DataNode> mapper = new HashMap<String, DataNode>();
	private static Preferences prefs;
	private String filePath;
	private String fileName;
	private String activeTab;
	private JScrollPane scrollPane_1;
	private JLabel lblNewLabel_2;
	private JFrame frame;
	private JTree tree;
	private MyKeyListener listener;
	private JTabbedPane tabbedPane;
	private JTextField textField;
	private JPanel panel_6;
	private boolean state;
	WordSearch ws = null;

	/**
	 * Launch the application.
	 * 
	 * @throws InterruptedException
	 * @throws InvocationTargetException
	 */
	public static void main(String[] args) throws InvocationTargetException, InterruptedException {
		EventQueue.invokeAndWait(new Runnable() {
			public void run() {
				try {
					prefs = Preferences.userRoot().node(this.getClass().getName());
					UIManager.setLookAndFeel("com.jtattoo.plaf." + prefs.get("Theme", "noire.Noire") + "LookAndFeel");
				} catch (ClassNotFoundException | InstantiationException | IllegalAccessException
						| UnsupportedLookAndFeelException e1) {
					e1.printStackTrace();
				}
				try {
					NotePad window = new NotePad();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public NotePad() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {

		frame = new JFrame();
		frame.setMinimumSize(new Dimension(400, 400));
		frame.setBounds(100, 100, 1054, 771);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		JPanel panel = new JPanel();
		frame.getContentPane().add(panel, BorderLayout.CENTER);
		panel.setLayout(new BorderLayout(0, 0));

		JPanel panel_2 = new JPanel();
		panel_2.setBorder(new LineBorder(Color.GRAY));
		panel.add(panel_2, BorderLayout.CENTER);
		panel_2.setLayout(new BorderLayout(0, 0));

		tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		tabbedPane.setBorder(null);
		panel_2.add(tabbedPane, BorderLayout.CENTER);

		JPanel panel_1 = new JPanel();
		panel_1.setPreferredSize(new Dimension(15, 15));
		panel_2.add(panel_1, BorderLayout.SOUTH);
		panel_1.setLayout(new BorderLayout(0, 0));

		lblNewLabel_2 = new JLabel("");
		lblNewLabel_2.setFont(new Font("DejaVu Sans Mono", Font.PLAIN, 12));

		panel_6 = new JPanel();
		panel_6.setVisible(state = false);
		panel_2.add(panel_6, BorderLayout.SOUTH);
		panel_6.setLayout(new MigLayout("", "[grow][][][]", "[15px]"));

		textField = new JTextField();
		panel_6.add(textField, "cell 0 0,growx");
		textField.setColumns(10);
		 
		JButton chckbxExact = new JButton("Find");
		chckbxExact.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if (!textField.getText().contentEquals("")) {
					DataNode dn = null;
					for (String key : mapper.keySet()) {
						if (key.contentEquals(activeTab)) {
							dn = mapper.get(key); 
							try {
								search(dn, textField);
							} catch (BadLocationException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
					}
				}
			}
		});
		panel_6.add(chckbxExact, "cell 1 0");

		JButton lblFind = new JButton("");
		lblFind.setIcon(new ImageIcon(NotePad.class.getResource("/resources/up.png")));
		lblFind.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
					try {
						ws.backwards();
					} catch (BadLocationException e) { 
						e.printStackTrace();
					}
			}
		});
		panel_6.add(lblFind, "cell 2 0,alignx trailing");

		JButton btnFind = new JButton("");
		btnFind.setIcon(new ImageIcon(NotePad.class.getResource("/resources/down.png")));
		btnFind.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				try {
					ws.forward();
				} catch (BadLocationException e) { 
					e.printStackTrace();
				}
			}
		});

		panel_6.add(btnFind, "cell 3 0");

		ChangeListener changeListener = new ChangeListener() {
			public void stateChanged(ChangeEvent changeEvent) {
				panel_6.setVisible(state = false);
				changeEventTab(changeEvent);
			}
		};
		tabbedPane.addChangeListener(changeListener);

		JPanel panel_3 = new JPanel();
		panel_3.setPreferredSize(new Dimension(250, 10));
		panel_3.setBackground(Color.LIGHT_GRAY);
		panel_3.setBorder(new LineBorder(Color.GRAY));
		panel.add(panel_3, BorderLayout.WEST);
		panel_3.setLayout(new BorderLayout(0, 0));

		JPanel panel_4 = new JPanel();
		panel_3.add(panel_4, BorderLayout.CENTER);
		panel_4.setLayout(new MigLayout("", "[grow]", "[][grow][grow]"));

		JPanel panel_5 = new JPanel();
		panel_4.add(panel_5, "cell 0 0,grow");
		panel_5.setLayout(new BorderLayout(0, 0));

		JLabel lblPwd = new JLabel("Working Directory");
		panel_5.add(lblPwd, BorderLayout.CENTER);

		JButton btnChangePwd = new JButton("Set");
		btnChangePwd.setToolTipText("Set the location of the working directory.");
		btnChangePwd.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				setTreeWD("chooseFile");
			}
		});
		panel_5.add(btnChangePwd, BorderLayout.EAST);

		scrollPane_1 = new JScrollPane();
		panel_4.add(scrollPane_1, "cell 0 1 1 2,grow");

		scrollPane_1.setViewportView(tree);
		scrollPane_1.setFocusable(false);

		JPanel panel_7 = new JPanel();
		panel_7.setLayout(new MigLayout("", "[grow][grow][grow]", "[]"));

		JLabel lblNewLabel = new JLabel("Tree");
		panel_7.add(lblNewLabel, "cell 0 0 2 1");

		JPanel panel_8 = new JPanel();
		panel_8.setBorder(new TitledBorder(null, "File stuff", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		panel_3.add(panel_8, BorderLayout.SOUTH);
		panel_8.setLayout(new GridLayout(2, 2, 0, 0));

		JButton btnNewButton_1 = new JButton("Open");
		panel_8.add(btnNewButton_1);
		btnNewButton_1.setToolTipText("Open highlighted file");
		btnNewButton_1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if (filePath != null && filePath != setOrGetPref("Root", null, "get")) {
					addTab(tabbedPane);
				} else {
					EventQueue.invokeLater(new Runnable() {
						@Override
						public void run() {
							JOptionPane.showMessageDialog(frame, "No File Highlighted!", "Hay",
									JOptionPane.ERROR_MESSAGE);
						}
					});
				}
			}
		});

		JButton btnNewButton_2 = new JButton("New");
		panel_8.add(btnNewButton_2);
		btnNewButton_2.setToolTipText("Create a new file");
		btnNewButton_2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				newFile();
			}
		});

		JButton btnNewFile = new JButton("Save");
		panel_8.add(btnNewFile);
		btnNewFile.setToolTipText("Save foreground tab");
		btnNewFile.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (activeTab != null && !activeTab.isEmpty()) {
					saveFile(mapper.get(activeTab));
				} else {
					EventQueue.invokeLater(new Runnable() {
						@Override
						public void run() {
							JOptionPane.showMessageDialog(frame, "No File to Save!", "Hay", JOptionPane.ERROR_MESSAGE);
						}
					});
				}
			}
		});

		JButton btnNewButton_3 = new JButton("Delete");
		panel_8.add(btnNewButton_3);
		btnNewButton_3.setToolTipText("Delete file from working directory");
		btnNewButton_3.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				deleteFile(tabbedPane);
			}
		});

		JMenuBar menuBar = new JMenuBar();
		frame.setJMenuBar(menuBar);

		JMenu mnNewMenu = new JMenu("Options");
		menuBar.add(mnNewMenu);

		JMenuItem mntmFile = new JMenuItem("Set Font");
		mntmFile.setMnemonic(KeyEvent.VK_E);
		mntmFile.setToolTipText("Set theme of Text Editor");
		mntmFile.addActionListener((ActionEvent event) -> {
			setFont();
		});
		mnNewMenu.add(mntmFile);

		JMenuItem mntmNewMenuItem = new JMenuItem("Set Theme");
		mntmNewMenuItem.setMnemonic(KeyEvent.VK_E);
		mntmNewMenuItem.setToolTipText("Set theme of Text Editor");
		mntmNewMenuItem.addActionListener((ActionEvent event) -> {
			setTheme();
		});
		mnNewMenu.add(mntmNewMenuItem);

		JMenuItem mntmNewMenuItem_2 = new JMenuItem("Set Line Encoding");
		mnNewMenu.add(mntmNewMenuItem_2);
		mntmNewMenuItem.setMnemonic(KeyEvent.VK_E);
		mntmNewMenuItem.setToolTipText("Set theme of Text Editor");
		mntmNewMenuItem_2.addActionListener((ActionEvent event) -> {
			setLineEncoding();
		});

		JMenuItem mntmNewMenuItem_1 = new JMenuItem("Help");
		mntmNewMenuItem.setMnemonic(KeyEvent.VK_E);
		mntmNewMenuItem.setToolTipText("Set theme of Text Editor");
		mntmNewMenuItem_1.addActionListener((ActionEvent event) -> {
			EventQueue.invokeLater(new Runnable() {
				@Override
				public void run() {
					JOptionPane.showMessageDialog(frame, "No Help For You:", "Alert", JOptionPane.ERROR_MESSAGE);
				}
			});

		});
		mnNewMenu.add(mntmNewMenuItem_1);

		menuBar.add(Box.createHorizontalGlue());
		menuBar.add(lblNewLabel_2);

		setTreeWD("display");
		listener = new MyKeyListener();
		frame.setFocusable(false);
	}

	public void search(DataNode dn, JTextField textField2) throws BadLocationException {
		 
		ws = new WordSearch(dn, textField);
		ws.setFirst();
	}

//	protected void search(DataNode dn, JTextField jtfSearch, WordHighLighter wh) {
//
//		ArrayList<Integer> location = new ArrayList<Integer>();
//		Highlighter.HighlightPainter painter = new DefaultHighlighter.DefaultHighlightPainter(Color.cyan);
//
//		String word = jtfSearch.getText().trim();
//		String document = dn.getJta().getText();
//
//		int i = document.indexOf(word);
//		while (i >= 0) {
//			location.add(i);
//			i = document.indexOf(word, i + 1);
//		}
//		
//		wh = new WordHighLighter(word,  dn, location);
//		
////		System.out.println(location);
////		int j = dn.getJta().getCaretPosition();
////		for (int k = 0; k < location.size(); k++) {
////			if (location.get(k) >= dn.getJta().getCaretPosition()) {
////				try {
////					dn.getJta().getHighlighter().addHighlight(location.get(k), location.get(k) + word.length(),
////							painter);
////				} catch (BadLocationException e) {
////					e.printStackTrace();
////				}
////				return;
////			}
////		}
//
//		// for (int j = 0; j < location.size(); j++) {
//		// try {
//		// dn.getJta().getHighlighter().addHighlight(location.get(j), location.get(j) +
//		// word.length(), painter);
//		// } catch (BadLocationException ble) {
//		// System.out.println(ble);
//		// }
//		// }
//
//	}
	

	/**
	 * Monitors change in active tab 
	 */
	private void changeEventTab(ChangeEvent changeEvent) {
		JTabbedPane sourceTabbedPane = (JTabbedPane) changeEvent.getSource();
		int index = sourceTabbedPane.getSelectedIndex();
		if (index == -1) {
			activeTab = "";
		} else {
			String temp = activeTab;
			activeTab = sourceTabbedPane.getTitleAt(index);
			for (String key : mapper.keySet()) {
				if (key.contentEquals(temp)) {
					DataNode dn = mapper.get(key);
					dn.getJta().removeKeyListener(listener);
				} else if (key.contentEquals(activeTab)) {
					DataNode dn = mapper.get(key);
					dn.getJta().addKeyListener(listener);
				}
			}
		}
	}

	/**
	 * Create a new file
	 */
	private void newFile() {
		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				File fileToSave = null;
				boolean write = false;
				int reply = JOptionPane.showConfirmDialog(frame, "Save to working directory:", "Save",
						JOptionPane.YES_NO_OPTION);
				if (reply == JOptionPane.YES_OPTION) {
					String name = JOptionPane.showInputDialog(frame, "File Name:");
					if (name != null && !name.isEmpty()) {
						fileToSave = new File(setOrGetPref("Root", null, "get") + "/" + name);
						try {
							fileToSave.createNewFile();
						} catch (IOException e) {
							e.printStackTrace();
						}
						write = true;
					}
				} else if (reply == JOptionPane.NO_OPTION) {
					JFileChooser fileChooser = new JFileChooser();
					fileChooser.setDialogTitle("Specify save location.");
					int userSelection = fileChooser.showSaveDialog(frame);
					if (userSelection == JFileChooser.APPROVE_OPTION) {
						fileToSave = fileChooser.getSelectedFile();
						try {
							fileToSave.createNewFile();
							write = true;
						} catch (IOException e) {
							e.printStackTrace();
						}

					}
				}

				if ((reply == JOptionPane.NO_OPTION || reply == JOptionPane.YES_OPTION) && write == true) {
					filePath = fileToSave.toString();
					Path p = Paths.get(filePath);
					fileName = p.getFileName().toString();
					addTab(tabbedPane);
					setTreeWD("display");
				}
			}
		});
	}

	/**
	 * Sets the colour and size of font
	 */
	private void setFont() {
		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				JPanel myPanel = new JPanel();
				myPanel.setLayout(new MigLayout("", "[grow][grow]", "[grow][grow][grow][grow][grow]"));
				JLabel lblSetHteFont = new JLabel("Font Size:");
				myPanel.add(lblSetHteFont, "cell 0 1,alignx trailing");

				Integer[] ITEMS = { 9, 10, 11, 12, 14, 16, 18, 20, 24, 32 };
				JComboBox<Integer> comboBox = new JComboBox<Integer>(ITEMS);
				comboBox.getModel().setSelectedItem(Integer.parseInt(setOrGetPref("FontSize", null, "get")));
				myPanel.add(comboBox, "cell 1 1,growx");

				String fonts[] = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
				JLabel lblSetFontType = new JLabel("Font Type:");
				myPanel.add(lblSetFontType, "cell 0 2,alignx trailing");
				JComboBox<String> comboBox_3 = new JComboBox<String>(fonts);
				comboBox_3.getModel().setSelectedItem(setOrGetPref("FontType", null, "get"));
				myPanel.add(comboBox_3, "cell 1 2,growx");

				String[] colours = { "Black", "Blue", "Gray", "Green", "Orange", "Red", "White", "Yellow", "Pink" };
				JLabel lblSetFourgroun = new JLabel("Foreground Colour:");
				myPanel.add(lblSetFourgroun, "cell 0 3,alignx trailing");
				JComboBox<String> comboBox_1 = new JComboBox<String>(colours);
				comboBox_1.getModel().setSelectedItem(setOrGetPref("FourgColour", null, "get"));
				myPanel.add(comboBox_1, "cell 1 3,growx");

				JLabel lblSetBackgroundColour = new JLabel("Background Colour:");
				myPanel.add(lblSetBackgroundColour, "cell 0 4,alignx trailing");
				JComboBox<String> comboBox_2 = new JComboBox<String>(colours);
				comboBox_2.getModel().setSelectedItem(setOrGetPref("BackColour", null, "get"));
				myPanel.add(comboBox_2, "cell 1 4,growx");

				int result = JOptionPane.showConfirmDialog(frame, myPanel, "Set Font", JOptionPane.OK_CANCEL_OPTION);
				if (result == JOptionPane.OK_OPTION) {
					Integer temp = (Integer) comboBox.getSelectedItem();
					setOrGetPref("FontSize", Integer.toString(temp), "set");
					setOrGetPref("FourgColour", (String) comboBox_1.getSelectedItem(), "set");
					setOrGetPref("BackColour", (String) comboBox_2.getSelectedItem(), "set");
					setOrGetPref("FontType", (String) comboBox_3.getSelectedItem(), "set");
					for (DataNode dn : mapper.values()) {
						dn.changeFont(Integer.toString(temp), (String) comboBox_1.getSelectedItem(),
								(String) comboBox_2.getSelectedItem(), (String) comboBox_3.getSelectedItem());
					}
				}
			}
		});
	}

	/**
	 * Sets the theme of the window
	 */
	private void setTheme() {
		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				String[] values = { "Aluminium", "Smart", "Noire", "Acryl", "Aero", "Fast", "HiFi", "Texture", "McWin",
						"Mint", "Bernstein", "Luna" };
				Object selected = JOptionPane.showInputDialog(frame, "Choose Your  Theme", "Selection",
						JOptionPane.DEFAULT_OPTION, null, values, setOrGetPref("Theme", null, "get")
								.substring(setOrGetPref("Theme", null, "get").lastIndexOf(".") + 1));
				if (selected != null) {
					setOrGetPref("Theme", selected.toString().toLowerCase() + "." + selected.toString(), "set");
					try {
						UIManager.setLookAndFeel(
								"com.jtattoo.plaf." + prefs.get("Theme", "noire.Noire") + "LookAndFeel");
					} catch (ClassNotFoundException | InstantiationException | IllegalAccessException
							| UnsupportedLookAndFeelException e1) {
						e1.printStackTrace();
					}
					frame.setVisible(false);
					SwingUtilities.updateComponentTreeUI(frame);
					frame.setVisible(true);
				}
			}
		});
	}

	/**
	 * Sets line encoding of written file
	 */
	private void setLineEncoding() {
		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				if (JOptionPane.showConfirmDialog(frame, "Use Linux Line Encoding", "Choose Linux",
						JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
					setOrGetPref("LineEncoding", "Yes", "set");
				}
			}
		});
	}

	/**
	 * Deletes file from memory 
	 */
	protected void deleteFile(JTabbedPane tabbedPane) {
		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				if (fileName != null && !fileName.isEmpty()) {
					File file = new File(filePath);
					if (!file.isDirectory()) {
						if (fileName != null && !fileName.isEmpty()) {
							int reply = JOptionPane.showConfirmDialog(frame, "Delete Following File: " + fileName,
									"Delete", JOptionPane.YES_NO_OPTION);
							if (reply == JOptionPane.YES_OPTION) {
								if (file.delete()) {
									String temp = null;
									for (String key : mapper.keySet()) {
										if (fileName.contentEquals(key)) {
											for (int i = 0; i < tabbedPane.getTabCount(); i++) {
												if (tabbedPane.getTitleAt(i).equals(fileName))
													tabbedPane.remove(i);
												temp = key;
											}
										}
									}
									if (temp != null && !temp.isEmpty()) {
										mapper.remove(temp);
										filePath = setOrGetPref("Root", null, "get");
									}
									setTreeWD("display");
								} else
									JOptionPane.showMessageDialog(frame, "Error file not Deleted:");
							}
						} else
							JOptionPane.showMessageDialog(frame, "No File to Delete!");

					} else
						JOptionPane.showMessageDialog(frame, "No File to Delete!");
				} else
					JOptionPane.showMessageDialog(frame, "No File to Delete!");
			}
		});
	}

	/**
	 * Builds a JTree 
	 */
	private static DefaultMutableTreeNode addNodes(File dir) {
		DefaultMutableTreeNode node = new DefaultMutableTreeNode(dir);
		for (File file : dir.listFiles()) {
			if (file.isDirectory()) {
				node.add(addNodes(file));
			} else {
				node.add(new DefaultMutableTreeNode(file));
			}
		}
		return node;
	}

	/**
	 * Reads text form a JTextArea and writes to specified file.
	 */
	private void saveFile(DataNode dataNode) {
		try (BufferedWriter bw = Files.newBufferedWriter(Paths.get(dataNode.getLocation()))) {
			if (setOrGetPref("LineEncoding", null, "get").contentEquals("Yes")) {
				bw.write(dataNode.getJta().getText());
			} else
				dataNode.getJta().write(bw);
			Path p = Paths.get(dataNode.getLocation());
			displayInfo("Saved: " + p.getFileName().toString());
			bw.flush();
		} catch (Exception e) {
			System.err.println("Error: " + e.getMessage());
		}

	}

	/**
	 * Checks for user settings, returns setting if present
	 */
	private String setOrGetPref(String id, String value, String SetGet) {
		if (SetGet.equals("get")) {
			String temp = null;
			switch (id) {
			case "Root":
				temp = prefs.get(id, ".");
				break;
			case "FontSize":
				temp = prefs.get(id, "14");
				break;
			case "FourgColour":
				temp = prefs.get(id, "Black");
				break;
			case "BackColour":
				temp = prefs.get(id, "White");
				break;
			case "Theme":
				temp = prefs.get(id, "noire.Noire");
				break;
			case "FontType":
				temp = prefs.get(id, "Calibri");
				break;
			case "LineEncoding":
				temp = prefs.get(id, "No");
				break;
			default:
				break;
			}
			return temp;
		} else
			prefs.put(id, value);
		return "";
	}

	/**
	 * Adds a new tab to the JTabPane, with a close button.
	 */
	private void addTab(JTabbedPane tabbedPane) {
		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {

				for (String name : mapper.keySet()) {
					if (name.contentEquals(fileName)) {
						for (int i = 0; i < tabbedPane.getTabCount(); i++) {
							if (tabbedPane.getTitleAt(i).equals(fileName))
								tabbedPane.setSelectedIndex(i);
						}
						return;
					}
				}
				String line;
				DataNode dn = new DataNode(filePath, setOrGetPref("FontSize", null, "get"),
						setOrGetPref("FourgColour", null, "get"), setOrGetPref("BackColour", null, "get"),
						setOrGetPref("FontType", null, "get"));
				tabbedPane.addTab(fileName, null, dn.getJs());
				tabbedPane.setTabComponentAt(tabbedPane.getTabCount() - 1,
						new ButtonTabComponent(tabbedPane, mapper, fileName));
				tabbedPane.setSelectedIndex(tabbedPane.getTabCount() - 1);
				try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
					while ((line = br.readLine()) != null) {
						dn.getJta().append(line + "\n");
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
				dn.getJta().addKeyListener(listener);

				dn.getJta().setCaretPosition(0);
				mapper.put(fileName, dn);
			}
		});
	}

	/**
	 * Displays info about file saved
	 */
	private void displayInfo(String info) {
		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				Integer waitSeconds = 3;
				lblNewLabel_2.setIcon(new ImageIcon(NotePad.class.getResource("/resources/ic_penguin.gif")));
				Timer timer = new Timer(waitSeconds * 1000, new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						lblNewLabel_2.setText("");
						lblNewLabel_2.setIcon(null);
					}
				});
				timer.start();
			}
		});
	}

	/**
	 * Sets the tree view directory for user.
	 */
	private void setTreeWD(String type) {
		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				if (type.contentEquals("chooseFile")) {
					JFileChooser f = new JFileChooser();
					f.setDialogTitle("Specify Your Working Directory");
					f.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
					f.showSaveDialog(null);
					if (f.getSelectedFile() != null) {
						setOrGetPref("Root", f.getSelectedFile().toString(), "set");
						tree = new JTree(addNodes(new File(f.getSelectedFile().toString())));
					} else {
						return;
					}
				} else
					tree = new JTree(addNodes(new File(setOrGetPref("Root", null, "get"))));

				tree.setRootVisible(true);
				tree.setShowsRootHandles(false);
				tree.setToggleClickCount(10);
				tree.setFocusable(false);
				tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
				tree.setCellRenderer(new FileTreeCellRenderer());
				scrollPane_1.setViewportView(tree);
				tree.addTreeSelectionListener(new TreeSelectionListener() {
					@Override
					public void valueChanged(TreeSelectionEvent e) {
						DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) tree
								.getLastSelectedPathComponent();
						Object userObject = selectedNode.getUserObject();
						filePath = userObject.toString();
						Path p = Paths.get(filePath);
						fileName = p.getFileName().toString();
					}
				});

				MouseListener ml = new MouseAdapter() {
					public void mousePressed(MouseEvent e) {
						int selRow = tree.getRowForLocation(e.getX(), e.getY());
						if (selRow != -1) {
							if (e.getClickCount() == 2) {
								if (filePath != null && filePath != setOrGetPref("Root", null, "get")) {
									addTab(tabbedPane);
								}
							}
						}
					}
				};
				tree.addMouseListener(ml);
			}
		});
	}

	/**
	 * Helper class KeyListener
	 */
	public class MyKeyListener implements KeyListener {

		boolean ctl = false;
		boolean s = false;
		boolean f = false;

		@Override
		public void keyTyped(KeyEvent e) {
		}

		@Override
		public void keyPressed(KeyEvent e) {
			switch (e.getKeyCode()) {
			case KeyEvent.VK_CONTROL:
				ctl = true;
				break;
			case KeyEvent.VK_S:
				s = true;
				break;
			case KeyEvent.VK_F:
				f = true;
				break;
			}
		}

		@Override
		public void keyReleased(KeyEvent e) {
			if (ctl == true && s == true) {
				if (activeTab != null && !activeTab.isEmpty()) {
					saveFile(mapper.get(activeTab));

				} else
					JOptionPane.showMessageDialog(frame, "No File to Save!");
			} else if (ctl == true && f == true) {
				state = !state;
				panel_6.setVisible(state);
			}

			switch (e.getKeyCode()) {
			case KeyEvent.VK_CONTROL:
				ctl = false;
				break;
			case KeyEvent.VK_S:
				s = false;
				break;
			case KeyEvent.VK_F:
				f = false;
				break;
			default:
				s = false;
				ctl = false;
				f = false;
				break;
			}
		}
	}

	/**
	 * Helper class for JTree Creation
	 */
	class FileTreeCellRenderer extends DefaultTreeCellRenderer {
		private static final long serialVersionUID = 1L;

		@Override
		public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded,
				boolean leaf, int row, boolean hasFocus) {
			if (value instanceof DefaultMutableTreeNode) {
				value = ((DefaultMutableTreeNode) value).getUserObject();
				if (value instanceof File) {
					value = ((File) value).getName();
				}
			}
			return super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
		}
	}
}