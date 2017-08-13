package pfGUI;

import java.awt.Color;
import java.awt.EventQueue;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JButton;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import javax.swing.JTextField;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.util.LinkedList;
import java.awt.event.ActionEvent;
import javax.swing.JLabel;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.LayoutStyle.ComponentPlacement;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JTextArea;
import javax.swing.JCheckBox;
import javax.swing.event.ChangeListener;
import particlefilter.FileWrite;

import javax.swing.event.ChangeEvent;

/**
 * Encouraged by https://github.com/erhs-53-hackers/Particle-Filter/tree/master/src/particlefilter
 * 
 * @author LeslieXong
 *
 */
public class pfTest extends JFrame
{
	private static final long serialVersionUID = 1L;

	ParticleFilter particleFilter;
	ParticleFilter particleFilterMle;
	//Use this to get EAP position of distance measurement alone,without INS

	final Point[] landmarks = new Point[] { new Point(105, 160f), new Point(625, 420f),
			new Point(225f, 400f), new Point(399f, 250f),
			new Point(630, 140f), new Point(337f, 42f) };

	final int PARTICLES_NUM = 3000;
	final int WORLD_WIDTH = 750, WORLD_HEIGHT = 500;
	final int G_MARGIN_X = 12, G_MARGIN_Y = 35;
	final float SCALE=10f;  //pixel and meter

	private Image image;
	private Graphics2D graphics;

	private JPanel contentPane;
	private JTextField textFieldMoveNoise;
	private JTextField textFieldTurnNoise;
	private JTextField textFieldSenseNoise;
	private JTextArea txtNotice;

	//default display
	private boolean show_MAP = false;
	private boolean show_EAP = true;
	private boolean show_Ldmk = false;
	private boolean show_particles = true;
	private boolean show_INS = true;

	private float moveNoise = 1;
	private float orientNoise = 2;
	private float senseNoise = 3;

	private LinkedList<Point> truePosition = new LinkedList<Point>();
	private LinkedList<Point> mapPosition = new LinkedList<Point>();
	private LinkedList<Point> eapPosition = new LinkedList<Point>();
	private LinkedList<Point> mlePosition = new LinkedList<Point>();
	private LinkedList<Point> insPosition = new LinkedList<Point>();

	private Particle insParticle;
	private float insError = 0;//
	private float eapError = 0;
	private float mapError = 0;
	private float mleError = 0;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args)
	{
		EventQueue.invokeLater(new Runnable()
		{
			public void run()
			{
				try
				{
					pfTest frame = new pfTest();
					frame.setVisible(true);
				} catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		});
	}

	private void setUp()
	{
		insError = 0;
		eapError = 0;
		mapError = 0;
		mleError = 0;

		truePosition = new LinkedList<Point>();
		mapPosition = new LinkedList<Point>();
		eapPosition = new LinkedList<Point>();
		mlePosition = new LinkedList<Point>();
		insPosition = new LinkedList<>();

		insParticle = new Particle(landmarks, WORLD_WIDTH, WORLD_HEIGHT);

		image = new BufferedImage(WORLD_WIDTH, WORLD_HEIGHT, BufferedImage.BITMASK);
		graphics = (Graphics2D) image.getGraphics();

		particleFilter = new ParticleFilter(PARTICLES_NUM, landmarks, WORLD_WIDTH, WORLD_HEIGHT);
		particleFilter.setNoise(moveNoise, orientNoise, senseNoise);

		particleFilterMle = new ParticleFilter(PARTICLES_NUM, landmarks, WORLD_WIDTH, WORLD_HEIGHT);
		particleFilterMle.setNoise(moveNoise, orientNoise, senseNoise);

		graphics.drawRect(0, 0, WORLD_WIDTH - 1, WORLD_HEIGHT - 1);
	}

	@Override
	public void update(Graphics g)
	{
		paint(g);
	}

	@Override
	public void paint(Graphics g)
	{
		super.paint(g);
		
		/* Draw simulation environment ****************************************/
		graphics.clearRect(0, 0, WORLD_WIDTH, WORLD_HEIGHT);
		graphics.setPaint(Color.white);
		graphics.drawRect(0, 0, WORLD_WIDTH - 1, WORLD_HEIGHT - 1);

		
		graphics.setPaint(Color.red);
		graphics.drawString(String.format("X( Width ): %.1fm", WORLD_WIDTH/SCALE), 10, 20);
		graphics.drawString(String.format("Y( Height ):%.1fm", WORLD_HEIGHT/SCALE), 10, 40);
		
		/* Draw all particles *************************************************/
		if (show_particles)
		{
			graphics.setPaint(Color.PINK);
			for (Particle p : particleFilter.particles)
			{
				graphics.drawRect((int) p.x, (int) p.y, 1, 1);
			}
		}

		/* Draw all landmarks *************************************************/
		if (show_Ldmk)
		{
			graphics.setPaint(Color.YELLOW);
			for (Point p : landmarks)
			{
				graphics.fillOval((int) p.x - 10, (int) p.y - 10, 15, 15);
			}
		}

		/* Draw the robot *****************************************************/
		graphics.setPaint(Color.RED);
		for (Point p : truePosition)
		{
			graphics.fillOval((int) p.x - 5, (int) p.y - 5, 10, 10);
		}

		/* Show the best particle *********************************************/
		if (show_MAP)
		{
			graphics.setPaint(Color.GREEN);
			for (Point p : mapPosition)
			{
				graphics.fillOval((int) p.x - 5, (int) p.y - 5, 10, 10);
			}
		}

		/* Show the average particle ******************************************/
		if (show_EAP)
		{
			graphics.setPaint(Color.BLUE);
			for (Point p : eapPosition)
			{
				graphics.fillOval((int) p.x - 5, (int) p.y - 5, 10, 10);
			}
		}
		
		/* Show the ins ******************************************/
		if (show_INS)
		{
			graphics.setPaint(Color.magenta);
			for (Point p : insPosition)
			{
				graphics.fillOval((int) p.x - 5, (int) p.y - 5, 10, 10);
			}
		}
		
		if (truePosition.size() != 0)
		{
			txtNotice.setText(
					String.format("MLE:%.2f\nINS:%.2f \nMAP:%.2f\nEAP:%.2f\n", mleError, insError, mapError, eapError));
		}

		g.drawImage(image, G_MARGIN_X, G_MARGIN_Y, rootPane);
	}

	private void saveResult()
	{
		txtNotice.setText("Saving...");
		String file="src/pfGUI/pf-out.csv";
		FileWrite fw = new FileWrite(file);
		String c=String.format("True,%.2f,MLE,%.2f,MAP,%.2f,EAP,%.2f,INS,%.2f\n", 0f,mleError,mapError,eapError,insError);
		fw.write(c);
		for (int i = 0; i < truePosition.size(); i++)
		{
			String s = String.format("%.2f,%.2f,%.2f,%.2f,%.2f,%.2f,%.2f,%.2f,%.2f,%.2f\n",
					truePosition.get(i).x/SCALE, truePosition.get(i).y/SCALE, mlePosition.get(i).x/SCALE, mlePosition.get(i).y/SCALE,
					mapPosition.get(i).x/SCALE, mapPosition.get(i).y/SCALE, eapPosition.get(i).x/SCALE, eapPosition.get(i).y/SCALE,
					insPosition.get(i).x/SCALE, insPosition.get(i).y/SCALE);
			fw.write(s);
		}
		fw.closeStream();
		txtNotice.setText("data saved in:\n"+file);
	}

	private void updateNoise()
	{
		moveNoise =SCALE* Float.parseFloat(textFieldMoveNoise.getText());
		orientNoise = (float) Math.toRadians(Double.parseDouble(textFieldTurnNoise.getText()));
		senseNoise = SCALE*Float.parseFloat(textFieldSenseNoise.getText());

		particleFilter.setNoise(moveNoise, orientNoise, senseNoise);
		insParticle.setNoise(moveNoise, orientNoise, senseNoise);
		particleFilterMle.setNoise(moveNoise, orientNoise, senseNoise);
	}

	/**
	 * Use distance measurement to estimate the position
	 * 
	 * @param Z
	 * @return
	 */
	private Particle getDisAloneEstimation(float[] Z)
	{
		particleFilterMle = new ParticleFilter(PARTICLES_NUM, landmarks, WORLD_WIDTH, WORLD_HEIGHT);
		particleFilterMle.setNoise(moveNoise, orientNoise, senseNoise);

		try
		{
			particleFilterMle.reSample(Z);
			return particleFilterMle.getAverageParticle();
		} catch (Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Create the frame.
	 */
	public pfTest()
	{
		setUp();
		setTitle("Particle filter Simulator(By LeslieXong)");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 1000, 550);
		contentPane = new JPanel();

		//choose a new position
		contentPane.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mouseClicked(MouseEvent e)
			{
				updateNoise();

				float x = e.getX();
				float y = e.getY();

				try
				{
					if (truePosition.size() != 0)
					{
						float deltax = x - truePosition.getLast().x;
						float deltay = y - truePosition.getLast().y;
						float foward = (float) Math.sqrt(deltax * deltax + deltay * deltay);
						float direction = (float) Math.atan2(deltay, deltax);

						insParticle.move(direction, foward);
						insError += (Utils.distance(insParticle.x, insParticle.y, x, y) - insError)
								/ (truePosition.size() + 1)/SCALE;
						insPosition.add(new Point(insParticle.x, insParticle.y));

						particleFilter.move(direction, foward);
					} else
					{
						insParticle.set(x, y, 0, 0);//initial
						insPosition.add(new Point(x, y));
					}

					Particle p = new Particle(landmarks, WORLD_WIDTH, WORLD_HEIGHT);
					p.setNoise(moveNoise, orientNoise, senseNoise);
					p.set(x, y, 0, 0);
					float[] Z = p.simulateSense();
					particleFilter.reSample(Z);

					truePosition.add(new Point(x, y));

					p = getDisAloneEstimation(Z);
					mlePosition.add(new Point(p.x, p.y));
					mleError += (Utils.distance(p.x, p.y, x, y) - mleError) / truePosition.size()/SCALE;

					p = particleFilter.getAverageParticle();
					eapPosition.add(new Point(p.x, p.y));
					eapError += (Utils.distance(p.x, p.y, x, y) - eapError) / truePosition.size()/SCALE;

					p = particleFilter.getBestParticle();
					mapPosition.add(new Point(p.x, p.y));
					mapError += (Utils.distance(p.x, p.y, x, y) - mapError) / truePosition.size()/SCALE;
				} catch (Exception ex)
				{
					System.out.println(ex.getMessage());
				}
				pfTest.this.repaint();
			}
		});

		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);

		JLabel lblNoiseSet = new JLabel("Noise std set:");

		JLabel lblMoveNiose = new JLabel("Move Noise(meter)");
		textFieldMoveNoise = new JTextField();
		textFieldMoveNoise.setToolTipText("Meters");
		textFieldMoveNoise.setText(String.valueOf(moveNoise));
		textFieldMoveNoise.setColumns(10);

		JLabel lblOrientNoise = new JLabel("Orient Noise(degree)");
		textFieldTurnNoise = new JTextField();
		textFieldTurnNoise.setToolTipText("Degree");
		textFieldTurnNoise.setText(String.valueOf(orientNoise));
		textFieldTurnNoise.setColumns(10);

		JLabel lblSenseNoise = new JLabel("Sense Noise(meter)");
		textFieldSenseNoise = new JTextField();
		textFieldSenseNoise.setToolTipText("Meters");
		textFieldSenseNoise.setText(String.valueOf(senseNoise));
		textFieldSenseNoise.setColumns(10);

		JButton btnSave = new JButton("Save Result");
		btnSave.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				saveResult();
			}
		});

		JButton btnReset = new JButton("Reset");
		btnReset.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				setUp();
				pfTest.this.repaint();
			}
		});

		JLabel lblShow = new JLabel("Display set:");

		final JCheckBox chckbxMap = new JCheckBox("MAP");
		chckbxMap.setBackground(Color.GREEN);
		chckbxMap.addChangeListener(new ChangeListener()
		{
			public void stateChanged(ChangeEvent e)
			{
				show_MAP = chckbxMap.isSelected();
				pfTest.this.repaint();
			}
		});
		chckbxMap.setSelected(show_MAP);

		final JCheckBox chckbxEap = new JCheckBox("EAP");
		chckbxEap.setForeground(Color.WHITE);
		chckbxEap.setBackground(Color.BLUE);
		chckbxEap.addChangeListener(new ChangeListener()
		{
			public void stateChanged(ChangeEvent e)
			{
				show_EAP = chckbxEap.isSelected();
				pfTest.this.repaint();
			}
		});
		chckbxEap.setSelected(show_EAP);

		final JCheckBox chckbxLandmark = new JCheckBox("LandMark");
		chckbxLandmark.setBackground(Color.YELLOW);
		chckbxLandmark.addChangeListener(new ChangeListener()
		{
			public void stateChanged(ChangeEvent e)
			{
				show_Ldmk = chckbxLandmark.isSelected();
				pfTest.this.repaint();
			}
		});
		chckbxLandmark.setSelected(show_Ldmk);

		final JCheckBox chckbxParticles = new JCheckBox("Particles");
		chckbxParticles.setBackground(Color.PINK);
		chckbxParticles.addChangeListener(new ChangeListener()
		{
			public void stateChanged(ChangeEvent e)
			{
				show_particles = chckbxParticles.isSelected();
				pfTest.this.repaint();
			}
		});
		chckbxParticles.setSelected(show_particles);

		txtNotice = new JTextArea();
		txtNotice.setText(
				"Notice:\n   Click on graphic to create a \ntrajectory to simulate tracking.");
		
		final JCheckBox chckbxIns = new JCheckBox("INS");
		chckbxIns.setBackground(Color.MAGENTA);
		chckbxIns.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				show_INS=chckbxIns.isSelected();
				pfTest.this.repaint();
			}
		});
		chckbxIns.setSelected(show_INS);
	

		GroupLayout gl_contentPane = new GroupLayout(contentPane);
		gl_contentPane.setHorizontalGroup(
			gl_contentPane.createParallelGroup(Alignment.TRAILING)
				.addGroup(gl_contentPane.createSequentialGroup()
					.addContainerGap(774, Short.MAX_VALUE)
					.addGroup(gl_contentPane.createParallelGroup(Alignment.LEADING)
						.addComponent(txtNotice, GroupLayout.PREFERRED_SIZE, 180, GroupLayout.PREFERRED_SIZE)
						.addComponent(lblShow)
						.addComponent(chckbxMap)
						.addComponent(chckbxEap)
						.addComponent(chckbxIns)
						.addGroup(gl_contentPane.createParallelGroup(Alignment.TRAILING)
							.addGroup(gl_contentPane.createSequentialGroup()
								.addGroup(gl_contentPane.createParallelGroup(Alignment.TRAILING, false)
									.addComponent(btnReset, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
									.addComponent(btnSave, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 115, Short.MAX_VALUE))
								.addGap(75))
							.addGroup(gl_contentPane.createParallelGroup(Alignment.TRAILING)
								.addGroup(gl_contentPane.createSequentialGroup()
									.addGroup(gl_contentPane.createParallelGroup(Alignment.TRAILING, false)
										.addComponent(lblSenseNoise, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
										.addComponent(lblMoveNiose, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
										.addComponent(lblOrientNoise, Alignment.LEADING))
									.addGap(33)
									.addGroup(gl_contentPane.createParallelGroup(Alignment.LEADING, false)
										.addComponent(textFieldSenseNoise, 0, 0, Short.MAX_VALUE)
										.addComponent(textFieldTurnNoise, GroupLayout.PREFERRED_SIZE, 37, GroupLayout.PREFERRED_SIZE)))
								.addComponent(textFieldMoveNoise, GroupLayout.PREFERRED_SIZE, 37, GroupLayout.PREFERRED_SIZE)
								.addComponent(lblNoiseSet, Alignment.LEADING)))
						.addGroup(gl_contentPane.createParallelGroup(Alignment.TRAILING, false)
							.addComponent(chckbxParticles, Alignment.LEADING, 0, 0, Short.MAX_VALUE)
							.addComponent(chckbxLandmark, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
					.addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
		);
		gl_contentPane.setVerticalGroup(
			gl_contentPane.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_contentPane.createSequentialGroup()
					.addComponent(lblNoiseSet)
					.addPreferredGap(ComponentPlacement.UNRELATED)
					.addGroup(gl_contentPane.createParallelGroup(Alignment.BASELINE)
						.addComponent(textFieldMoveNoise, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(lblMoveNiose))
					.addPreferredGap(ComponentPlacement.UNRELATED)
					.addGroup(gl_contentPane.createParallelGroup(Alignment.BASELINE)
						.addComponent(textFieldTurnNoise, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(lblOrientNoise))
					.addPreferredGap(ComponentPlacement.UNRELATED)
					.addGroup(gl_contentPane.createParallelGroup(Alignment.BASELINE)
						.addComponent(textFieldSenseNoise, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(lblSenseNoise))
					.addGap(33)
					.addComponent(lblShow)
					.addPreferredGap(ComponentPlacement.RELATED, 9, Short.MAX_VALUE)
					.addComponent(chckbxIns)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(chckbxMap)
					.addGap(1)
					.addComponent(chckbxEap)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(chckbxParticles)
					.addGap(2)
					.addComponent(chckbxLandmark)
					.addGap(26)
					.addComponent(btnSave)
					.addPreferredGap(ComponentPlacement.UNRELATED)
					.addComponent(btnReset)
					.addGap(18)
					.addComponent(txtNotice, GroupLayout.PREFERRED_SIZE, 76, GroupLayout.PREFERRED_SIZE)
					.addGap(42))
		);
		contentPane.setLayout(gl_contentPane);
	}
}
