package ui;

import java.awt.EventQueue;

import javax.swing.JInternalFrame;
import java.awt.BorderLayout;
import javax.swing.JLabel;
import net.miginfocom.swing.MigLayout;
import javax.swing.JSeparator;
import javax.swing.JPanel;
import java.awt.FlowLayout;
import java.awt.Font;

import javax.swing.JSplitPane;
import javax.swing.JComboBox;
import javax.swing.JSpinner;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.CardLayout;
import javax.swing.SpringLayout;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.RowSpec;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.BoxLayout;
import java.awt.GridLayout;
import javax.swing.JToggleButton;
import javax.swing.JList;
import javax.swing.JButton;
import javax.swing.JRadioButton;
import javax.swing.JCheckBox;
import java.awt.event.ActionListener;
import java.io.File;
import java.awt.event.ActionEvent;
import javax.swing.JProgressBar;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

public class FileImportMenus extends JInternalFrame {
	private JTextField textField;
	private JTextField textField_1;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					FileImportMenus frame = new FileImportMenus();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public FileImportMenus() {
		setBounds(100, 100, 640, 477);
		getContentPane().setLayout(new MigLayout("", "[426px,grow]", "[24px,top][][grow][grow][24px,center]"));
		
		JLabel titleText = new JLabel("Visualize new data");
		titleText.setFont(new Font("Lucida Grande", Font.PLAIN, 20));
		getContentPane().add(titleText, "cell 0 0,alignx center,aligny center");
		
		JSeparator separator = new JSeparator();
		getContentPane().add(separator, "cell 0 1");
		
		JPanel panel_2 = new JPanel();
		getContentPane().add(panel_2, "cell 0 3,grow");
		panel_2.setLayout(new BorderLayout(0, 0));
		
		JProgressBar loadingBar = new JProgressBar();
		panel_2.add(loadingBar, BorderLayout.CENTER);
		
		JPanel actionsPanel = new JPanel();
		getContentPane().add(actionsPanel, "flowx,cell 0 4,alignx center,growy");
		
		JButton cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
			}
		});
		actionsPanel.add(cancelButton);
		
		JButton doneButton = new JButton("Done");
		doneButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
			}
		});
		actionsPanel.add(doneButton);
		
		JSplitPane splitPane_1 = new JSplitPane();
		getContentPane().add(splitPane_1, "cell 0 2,grow");
		
		JPanel panel = new JPanel();
		splitPane_1.setLeftComponent(panel);
		GridBagLayout gbl_panel = new GridBagLayout();
		gbl_panel.columnWidths = new int[]{0, 0, 0};
		gbl_panel.rowHeights = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
		gbl_panel.columnWeights = new double[]{1.0, 1.0, Double.MIN_VALUE};
		gbl_panel.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, Double.MIN_VALUE};
		panel.setLayout(gbl_panel);
		
		JLabel lblNewLabel_2 = new JLabel("Settings");
		lblNewLabel_2.setFont(new Font("Lucida Grande", Font.BOLD, 13));
		GridBagConstraints gbc_lblNewLabel_2 = new GridBagConstraints();
		gbc_lblNewLabel_2.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel_2.gridx = 0;
		gbc_lblNewLabel_2.gridy = 0;
		panel.add(lblNewLabel_2, gbc_lblNewLabel_2);
		
		JComboBox<String> dimensionChooser = new JComboBox<String>();
		dimensionChooser.addItem("Overworld");
		dimensionChooser.addItem("Nether");
		dimensionChooser.addItem("End");
		dimensionChooser.setSelectedIndex(0);
		
		GridBagConstraints gbc_DataTypeChooser = new GridBagConstraints();
		gbc_DataTypeChooser.insets = new Insets(0, 0, 5, 5);
		gbc_DataTypeChooser.fill = GridBagConstraints.HORIZONTAL;
		gbc_DataTypeChooser.gridx = 0;
		gbc_DataTypeChooser.gridy = 1;
		panel.add(dimensionChooser, gbc_DataTypeChooser);
		
		JCheckBox convertChunksToggle = new JCheckBox("Convert Chunks");
		GridBagConstraints gbc_chckbxNewCheckBox = new GridBagConstraints();
		gbc_chckbxNewCheckBox.insets = new Insets(0, 0, 5, 5);
		gbc_chckbxNewCheckBox.gridx = 0;
		gbc_chckbxNewCheckBox.gridy = 2;
		panel.add(convertChunksToggle, gbc_chckbxNewCheckBox);
		
		JPanel maxEntriesPanel = new JPanel();
		GridBagConstraints gbc_maxEntriesPanel = new GridBagConstraints();
		gbc_maxEntriesPanel.insets = new Insets(0, 0, 5, 5);
		gbc_maxEntriesPanel.fill = GridBagConstraints.BOTH;
		gbc_maxEntriesPanel.gridx = 0;
		gbc_maxEntriesPanel.gridy = 3;
		panel.add(maxEntriesPanel, gbc_maxEntriesPanel);
		
		JSpinner maxEntriesSpinner = new JSpinner();
		maxEntriesSpinner.setValue(2500);
		maxEntriesPanel.add(maxEntriesSpinner);
		
		JLabel lblNewLabel_3 = new JLabel(" Max Entries");
		maxEntriesPanel.add(lblNewLabel_3);
		
		JLabel lblNewLabel_1 = new JLabel("Background Image");
		lblNewLabel_1.setFont(new Font("Lucida Grande", Font.BOLD, 13));
		GridBagConstraints gbc_lblNewLabel_1 = new GridBagConstraints();
		gbc_lblNewLabel_1.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel_1.gridx = 0;
		gbc_lblNewLabel_1.gridy = 6;
		panel.add(lblNewLabel_1, gbc_lblNewLabel_1);
		
		JPanel offsetPanel = new JPanel();
		GridBagConstraints gbc_splitPane = new GridBagConstraints();
		gbc_splitPane.insets = new Insets(0, 0, 5, 5);
		gbc_splitPane.fill = GridBagConstraints.VERTICAL;
		gbc_splitPane.gridx = 0;
		gbc_splitPane.gridy = 7;
		panel.add(offsetPanel, gbc_splitPane);
		
		textField_1 = new JTextField("X Offset");
		offsetPanel.add(textField_1);
		textField_1.setColumns(5);
		
		textField = new JTextField("Y Offset");
		offsetPanel.add(textField);
		textField.setColumns(5);
		
		JLabel lblNewLabel = new JLabel("Total Entries");
		GridBagConstraints gbc_lblNewLabel = new GridBagConstraints();
		gbc_lblNewLabel.insets = new Insets(0, 0, 0, 5);
		gbc_lblNewLabel.gridx = 0;
		gbc_lblNewLabel.gridy = 10;
		panel.add(lblNewLabel, gbc_lblNewLabel);
		
		JPanel panel_3 = new JPanel();
		splitPane_1.setRightComponent(panel_3);
		panel_3.setLayout(new BorderLayout(0, 0));
		
		JLabel fileListTitle = new JLabel("Input Files");
		gbc_lblNewLabel.insets = new Insets(0, 0, 0, 5);
		fileListTitle.setHorizontalAlignment(SwingConstants.CENTER);
		fileListTitle.setFont(new Font("Lucida Grande", Font.BOLD, 13));
		panel_3.add(fileListTitle, BorderLayout.NORTH);
		
		JButton addNewFilesButton = new JButton("Add Other Files");
		panel_3.add(addNewFilesButton, BorderLayout.SOUTH);
		addNewFilesButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
			}
		});
		
		JList<File> FileList = new JList<File>();
		panel_3.add(FileList, BorderLayout.CENTER);
	}
}
