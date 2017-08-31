package simulator;

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
import particlefilter.Particle;
import particlefilter.ParticleFilter;
import util.Matrix;
import util.Point2D;
import util.Point2D;

import javax.swing.event.ChangeEvent;

import kalmanfilter.*;

/**
 * Encouraged by https://github.com/erhs-53-hackers/Particle-Filter/tree/master/src/particlefilter
 * 
 * @author LeslieXong
 *
 */
public class Simulate extends JFrame
{
	private static final long serialVersionUID = 1L;

	private ParticleFilter particleFilter;

	private ParticleFilter mleEstimator;//Use this to get EAP position of distance measurement alone,without INS
	private Particle insEstimator;

	private KalmanFilter kalmanFilter;

	final Point2D[] landmarks = new Point2D[] { new Point2D(105, 160f), new Point2D(625, 420f),
			new Point2D(225f, 400f), new Point2D(399f, 250f),
			new Point2D(630, 140f), new Point2D(337f, 42f) };

	final int PARTICLES_NUM = 3000;
	final int WORLD_WIDTH = 750, WORLD_HEIGHT = 500;
	final int G_MARGIN_X = 12, G_MARGIN_Y = 35;
	final float SCALE = 10f;  //pixel per meter

	private Image image;
	private Graphics2D graphics;

	private JPanel contentPane;
	private JTextField textFieldMoveNoise;
	private JTextField textFieldTurnNoise;
	private JTextField textFieldSenseNoise;
	private JTextArea txtNotice;

	//default display
	private boolean showKF = true;
	private boolean showPF = true;

	private boolean showMLE = true;
	private boolean showINS = true;
	private boolean showParticles = true;

	private float moveNoise = 1;
	private float orientNoise = 2;
	private float senseNoise = 3;

	private LinkedList<Point2D> truePosition = new LinkedList<Point2D>();
	private LinkedList<Point2D> kfPosition = new LinkedList<Point2D>();
	private LinkedList<Point2D> pfPosition = new LinkedList<Point2D>();
	private LinkedList<Point2D> mlePosition = new LinkedList<Point2D>();
	private LinkedList<Point2D> insPosition = new LinkedList<Point2D>();

	private float insError = 0;
	private float pfError = 0;
	private float kfError = 0;
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
					Simulate frame = new Simulate();
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
		pfError = 0;
		kfError = 0;
		mleError = 0;

		truePosition = new LinkedList<Point2D>();
		kfPosition = new LinkedList<Point2D>();
		pfPosition = new LinkedList<Point2D>();
		mlePosition = new LinkedList<Point2D>();
		insPosition = new LinkedList<>();

		insEstimator = new Particle(landmarks, WORLD_WIDTH, WORLD_HEIGHT);
		insEstimator.setNoise(moveNoise, orientNoise, senseNoise);

		image = new BufferedImage(WORLD_WIDTH, WORLD_HEIGHT, BufferedImage.BITMASK);
		graphics = (Graphics2D) image.getGraphics();

		particleFilter = new ParticleFilter(PARTICLES_NUM, landmarks, WORLD_WIDTH, WORLD_HEIGHT);
		particleFilter.setNoise(moveNoise, orientNoise, senseNoise);

		mleEstimator = new ParticleFilter(PARTICLES_NUM, landmarks, WORLD_WIDTH, WORLD_HEIGHT);
		mleEstimator.setNoise(moveNoise, orientNoise, senseNoise);

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
		graphics.drawString(String.format("X( Width ): %.1fm", WORLD_WIDTH / SCALE), 10, 20);
		graphics.drawString(String.format("Y( Height ):%.1fm", WORLD_HEIGHT / SCALE), 10, 40);

		/* Draw all particles *************************************************/
		if (showParticles)
		{
			graphics.setPaint(Color.PINK);
			for (Particle p : particleFilter.particles)
			{
				graphics.drawRect((int) p.x, (int) p.y, 1, 1);
			}
		}

		/* Draw all landmarks *************************************************/
		graphics.setPaint(Color.CYAN);
		for (Point2D p : landmarks)
		{
			graphics.fillOval((int) p.x - 10, (int) p.y - 10, 15, 15);
		}

		if (showMLE)
		{
			graphics.setPaint(Color.YELLOW);
			for (Point2D p : mlePosition)
			{
				graphics.fillOval((int) p.x - 10, (int) p.y - 10, 15, 15);
			}
		}

		/* Draw the true trajectory ********************************************/
		graphics.setPaint(Color.RED);
		for (Point2D p : truePosition)
		{
			graphics.fillOval((int) p.x - 5, (int) p.y - 5, 10, 10);
		}

		/* Show the best particle *********************************************/
		if (showKF)
		{
			graphics.setPaint(Color.GREEN);
			for (Point2D p : kfPosition)
			{
				graphics.fillOval((int) p.x - 5, (int) p.y - 5, 10, 10);
			}
		}

		/* Show the average particle ******************************************/
		if (showPF)
		{
			graphics.setPaint(Color.BLUE);
			for (Point2D p : pfPosition)
			{
				graphics.fillOval((int) p.x - 5, (int) p.y - 5, 10, 10);
			}
		}

		/* Show the ins ******************************************/
		if (showINS)
		{
			graphics.setPaint(Color.magenta);
			for (Point2D p : insPosition)
			{
				graphics.fillOval((int) p.x - 5, (int) p.y - 5, 10, 10);
			}
		}

		if (truePosition.size() != 0)
		{
			txtNotice.setText(
					String.format("MLE:%.2f\nINS:%.2f \nKF:%.2f\nPF:%.2f\n", mleError, insError, kfError, pfError));
		}

		g.drawImage(image, G_MARGIN_X, G_MARGIN_Y, rootPane);
	}

	private void saveResult()
	{
		txtNotice.setText("Saving...");
		String file = "src/pfGUI/pf-out.csv";
		FileWrite fw = new FileWrite(file);
		String c = String.format("True,%.2f,MLE,%.2f,KF,%.2f,PF,%.2f,INS,%.2f\n", 0f, mleError, kfError, pfError,
				insError);
		fw.write(c);
		for (int i = 0; i < truePosition.size(); i++)
		{
			String s = String.format("%.2f,%.2f,%.2f,%.2f,%.2f,%.2f,%.2f,%.2f,%.2f,%.2f\n",
					truePosition.get(i).x / SCALE, truePosition.get(i).y / SCALE, mlePosition.get(i).x / SCALE,
					mlePosition.get(i).y / SCALE,
					kfPosition.get(i).x / SCALE, kfPosition.get(i).y / SCALE, pfPosition.get(i).x / SCALE,
					pfPosition.get(i).y / SCALE,
					insPosition.get(i).x / SCALE, insPosition.get(i).y / SCALE);
			fw.write(s);
		}
		fw.closeStream();
		txtNotice.setText("data saved in:\n" + file);
	}

	private void updateNoise()
	{
		moveNoise = SCALE * Float.parseFloat(textFieldMoveNoise.getText());
		orientNoise = (float) Math.toRadians(Double.parseDouble(textFieldTurnNoise.getText()));
		senseNoise = SCALE * Float.parseFloat(textFieldSenseNoise.getText());

		particleFilter.setNoise(moveNoise, orientNoise, senseNoise);
		insEstimator.setNoise(moveNoise, orientNoise, senseNoise);
		mleEstimator.setNoise(moveNoise, orientNoise, senseNoise);
	}

	/**
	 * Use distance measurement to estimate the position
	 * 
	 * @param Z
	 * @return
	 */
	private Particle getDisAloneEstimation(float[] Z)
	{
		mleEstimator = new ParticleFilter(PARTICLES_NUM, landmarks, WORLD_WIDTH, WORLD_HEIGHT);
		mleEstimator.setNoise(moveNoise, orientNoise, senseNoise);

		try
		{
			mleEstimator.reSample(Z);
			return mleEstimator.getEapPosition();
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
	public Simulate()
	{
		setUp();
		setTitle("Particle/Kalman filter Simulator(By lesliexong@qq.com)");
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

						insEstimator.move(direction, foward);
						insError += (Utils.distance(insEstimator.x, insEstimator.y, x, y) - insError)
								/ (truePosition.size() + 1) / SCALE;
						insPosition.add(new Point2D(insEstimator.x, insEstimator.y));

						particleFilter.move(direction, foward);

						double[][] pdr = { { deltax, 0 }, { 0, deltay } };
						kalmanFilter.setStateTransitModelF(new Matrix(pdr));
						//kalmanFilter.setProcessNoiseCovQ(foward*moveNoise); //Set transit Noise about distance.
					} else
					{
						insEstimator.set(x, y, 0, 0);//initial
						insPosition.add(new Point2D(x, y));
					}

					truePosition.add(new Point2D(x, y));

					Particle p = new Particle(landmarks, WORLD_WIDTH, WORLD_HEIGHT);
					p.setNoise(moveNoise, orientNoise, senseNoise);
					p.set(x, y, 0, 0);
					float[] Z = p.simulateSense();
					
					particleFilter.reSample(Z);
					p = particleFilter.getEapPosition();
					pfPosition.add(new Point2D(p.x, p.y));
					pfError += (Utils.distance(p.x, p.y, x, y) - pfError) / truePosition.size() / SCALE;

					p = getDisAloneEstimation(Z);
					mlePosition.add(new Point2D(p.x, p.y));
					mleError += (Utils.distance(p.x, p.y, x, y) - mleError) / truePosition.size() / SCALE;

					double[] mlePos = { p.x, p.y };
					Matrix zMatrix = new Matrix(mlePos).trans();
					
					if (kalmanFilter == null)
					{
						kalmanFilter = new KalmanFilter(2, 2);
						kalmanFilter.setCurrentState(zMatrix, new Matrix(2, 0.5)); //TODO prior error
					} else
					{
						//kalmanFilter.setObsvNoiseCovR(senseNoise);
						Matrix state = kalmanFilter.filter(zMatrix);
						Point2D pos = new Point2D(state.value(0, 0), state.value(1, 0));
						kfPosition.add(pos);
						kfError += (Utils.distance(pos.x, pos.y, x, y) - kfError) / truePosition.size() / SCALE;
					}
				} catch (Exception ex)
				{
					System.out.println(ex.getMessage());
				}
				Simulate.this.repaint();
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
				Simulate.this.repaint();
			}
		});

		JLabel lblShow = new JLabel("Display set:");

		final JCheckBox chckbxKF = new JCheckBox("Kalman Filter");
		chckbxKF.setBackground(Color.GREEN);
		chckbxKF.addChangeListener(new ChangeListener()
		{
			public void stateChanged(ChangeEvent e)
			{
				showKF = chckbxKF.isSelected();
				Simulate.this.repaint();
			}
		});
		chckbxKF.setSelected(showKF);

		final JCheckBox chckbxPF = new JCheckBox("Particle Filter");
		chckbxPF.setForeground(Color.WHITE);
		chckbxPF.setBackground(Color.BLUE);
		chckbxPF.addChangeListener(new ChangeListener()
		{
			public void stateChanged(ChangeEvent e)
			{
				showPF = chckbxPF.isSelected();
				Simulate.this.repaint();
			}
		});
		chckbxPF.setSelected(showPF);

		final JCheckBox chckbxMle = new JCheckBox("MLE Alone(triangle)");
		chckbxMle.setBackground(Color.YELLOW);
		chckbxMle.addChangeListener(new ChangeListener()
		{
			public void stateChanged(ChangeEvent e)
			{
				showMLE = chckbxMle.isSelected();
				Simulate.this.repaint();
			}
		});
		chckbxMle.setSelected(showMLE);

		final JCheckBox chckbxParticles = new JCheckBox("Particles");
		chckbxParticles.setBackground(Color.PINK);
		chckbxParticles.addChangeListener(new ChangeListener()
		{
			public void stateChanged(ChangeEvent e)
			{
				showParticles = chckbxParticles.isSelected();
				Simulate.this.repaint();
			}
		});
		chckbxParticles.setSelected(showParticles);

		txtNotice = new JTextArea();
		txtNotice.setText(
				"Notice:\n   Click on graphic step by step to \n create a trajectory for simulating \n tracking.");

		final JCheckBox chckbxIns = new JCheckBox("INS Alone");
		chckbxIns.setBackground(Color.MAGENTA);
		chckbxIns.addChangeListener(new ChangeListener()
		{
			public void stateChanged(ChangeEvent e)
			{
				showINS = chckbxIns.isSelected();
				Simulate.this.repaint();
			}
		});
		chckbxIns.setSelected(showINS);

		GroupLayout gl_contentPane = new GroupLayout(contentPane);
		gl_contentPane.setHorizontalGroup(
				gl_contentPane.createParallelGroup(Alignment.TRAILING)
						.addGroup(gl_contentPane.createSequentialGroup()
								.addContainerGap(774, Short.MAX_VALUE)
								.addGroup(gl_contentPane.createParallelGroup(Alignment.LEADING)
										.addComponent(txtNotice, GroupLayout.PREFERRED_SIZE, 180,
												GroupLayout.PREFERRED_SIZE)
										.addComponent(lblShow)
										.addComponent(chckbxKF)
										.addComponent(chckbxPF)
										.addComponent(chckbxIns)
										.addGroup(gl_contentPane.createParallelGroup(Alignment.TRAILING)
												.addGroup(gl_contentPane.createSequentialGroup()
														.addGroup(gl_contentPane
																.createParallelGroup(Alignment.TRAILING, false)
																.addComponent(btnReset, Alignment.LEADING,
																		GroupLayout.DEFAULT_SIZE,
																		GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
																.addComponent(btnSave, Alignment.LEADING,
																		GroupLayout.DEFAULT_SIZE, 115, Short.MAX_VALUE))
														.addGap(75))
												.addGroup(gl_contentPane.createParallelGroup(Alignment.TRAILING)
														.addGroup(gl_contentPane.createSequentialGroup()
																.addGroup(gl_contentPane
																		.createParallelGroup(Alignment.TRAILING, false)
																		.addComponent(lblSenseNoise, Alignment.LEADING,
																				GroupLayout.DEFAULT_SIZE,
																				GroupLayout.DEFAULT_SIZE,
																				Short.MAX_VALUE)
																		.addComponent(lblMoveNiose, Alignment.LEADING,
																				GroupLayout.DEFAULT_SIZE,
																				GroupLayout.DEFAULT_SIZE,
																				Short.MAX_VALUE)
																		.addComponent(lblOrientNoise,
																				Alignment.LEADING))
																.addGap(33)
																.addGroup(gl_contentPane
																		.createParallelGroup(Alignment.LEADING, false)
																		.addComponent(textFieldSenseNoise, 0, 0,
																				Short.MAX_VALUE)
																		.addComponent(textFieldTurnNoise,
																				GroupLayout.PREFERRED_SIZE, 37,
																				GroupLayout.PREFERRED_SIZE)))
														.addComponent(textFieldMoveNoise, GroupLayout.PREFERRED_SIZE,
																37, GroupLayout.PREFERRED_SIZE)
														.addComponent(lblNoiseSet, Alignment.LEADING)))
										.addGroup(gl_contentPane.createParallelGroup(Alignment.TRAILING, false)
												.addComponent(chckbxParticles, Alignment.LEADING, 0, 0, Short.MAX_VALUE)
												.addComponent(chckbxMle, Alignment.LEADING, GroupLayout.DEFAULT_SIZE,
														GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
								.addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));
		gl_contentPane.setVerticalGroup(
				gl_contentPane.createParallelGroup(Alignment.LEADING)
						.addGroup(gl_contentPane.createSequentialGroup()
								.addComponent(lblNoiseSet)
								.addPreferredGap(ComponentPlacement.UNRELATED)
								.addGroup(gl_contentPane.createParallelGroup(Alignment.BASELINE)
										.addComponent(textFieldMoveNoise, GroupLayout.PREFERRED_SIZE,
												GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
										.addComponent(lblMoveNiose))
								.addPreferredGap(ComponentPlacement.UNRELATED)
								.addGroup(gl_contentPane.createParallelGroup(Alignment.BASELINE)
										.addComponent(textFieldTurnNoise, GroupLayout.PREFERRED_SIZE,
												GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
										.addComponent(lblOrientNoise))
								.addPreferredGap(ComponentPlacement.UNRELATED)
								.addGroup(gl_contentPane.createParallelGroup(Alignment.BASELINE)
										.addComponent(textFieldSenseNoise, GroupLayout.PREFERRED_SIZE,
												GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
										.addComponent(lblSenseNoise))
								.addGap(33)
								.addComponent(lblShow)
								.addPreferredGap(ComponentPlacement.RELATED, 9, Short.MAX_VALUE)
								.addComponent(chckbxIns)
								.addPreferredGap(ComponentPlacement.RELATED)
								.addComponent(chckbxKF)
								.addGap(1)
								.addComponent(chckbxPF)
								.addPreferredGap(ComponentPlacement.RELATED)
								.addComponent(chckbxParticles)
								.addGap(2)
								.addComponent(chckbxMle)
								.addGap(26)
								.addComponent(btnSave)
								.addPreferredGap(ComponentPlacement.UNRELATED)
								.addComponent(btnReset)
								.addGap(18)
								.addComponent(txtNotice, GroupLayout.PREFERRED_SIZE, 76, GroupLayout.PREFERRED_SIZE)
								.addGap(42)));
		contentPane.setLayout(gl_contentPane);
	}
}
