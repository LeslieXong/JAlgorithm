package pfGUIcopy;

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
import java.awt.event.ActionEvent;
import javax.swing.JLabel;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.LayoutStyle.ComponentPlacement;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JTextArea;

/**
 * Main frame, borrowed from https://github.com/erhs-53-hackers/Particle-Filter/tree/master/src/particlefilter
 * 
 * @author LeslieXong
 *
 */
public class pfTest extends JFrame
{
	private static final long serialVersionUID = 1L;

	ParticleFilter particleFilter;
	Particle targetParticle;
	final Point[] landmarks = new Point[] { new Point(255, 220f), new Point(225, 40f),
			new Point(225f, 400f), new Point(559f, 280f),
			new Point(600 - 10f, 40f), new Point(117f, 92f) };
	
	final int PARTICLES_NUM = 10000;
	final int WORLD_WIDTH = 700, WORLD_HEIGHT = 500;
	final int G_MARGIN_X=10,G_MARGIN_Y=40;
	
	private Image image;
	private Graphics2D graphics;

	private JPanel contentPane;
	private JTextField textFieldDistance;
	private JTextField textFieldTurn;

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
		image = new BufferedImage(WORLD_WIDTH, WORLD_HEIGHT, BufferedImage.BITMASK);
		graphics = (Graphics2D) image.getGraphics();

		particleFilter = new ParticleFilter(PARTICLES_NUM, landmarks, WORLD_WIDTH, WORLD_HEIGHT);
		particleFilter.setNoise(0.55f, 0.15f, 20f);
		targetParticle = new Particle(landmarks, WORLD_WIDTH, WORLD_HEIGHT);
		
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

		/* Draw all particles *************************************************/
		graphics.setPaint(Color.PINK);
		for (Particle p : particleFilter.particles)
		{
			graphics.drawRect((int) p.x, (int) p.y, 1, 1);
		}

		/* Draw all landmarks *************************************************/
		graphics.setPaint(Color.YELLOW);
		for (Point p : landmarks)
		{
			graphics.fillOval((int) p.x - 10, (int) p.y - 10, 20, 20);
		}

		/* Draw the robot *****************************************************/
		graphics.setPaint(Color.RED);
		graphics.fillOval((int) targetParticle.x - 5, (int) targetParticle.y - 5, 10, 10);

		/* Show the best particle *********************************************/
		Particle p = particleFilter.getBestParticle();
		graphics.setPaint(Color.BLUE);
		graphics.fillOval((int) p.x - 5, (int) p.y - 5, 10, 10);

		/* Show the average particle ******************************************/
		p = particleFilter.getAverageParticle();
		graphics.setPaint(Color.GREEN);
		graphics.fillOval((int) p.x - 5, (int) p.y - 5, 10, 10);

		g.drawImage(image, G_MARGIN_X, G_MARGIN_Y, rootPane);
	}

	/**
	 * Create the frame.
	 */
	public pfTest()
	{
		setUp();
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 950, 550);
		contentPane = new JPanel();

		//choose a new position
		contentPane.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mouseClicked(MouseEvent e)
			{
				//int x = e.getX() - G_MARGIN_X;
				//int y = e.getY() - G_MARGIN_Y;
				int x = e.getX();
				int y = e.getY() ;
				try
				{
					targetParticle.set(x, y, 0, 0);
					float[] Z = targetParticle.sense();
					String s = "[";
					for (float f : Z)
					{
						s += f + ", ";
					}
					s += "]";
					System.out.println("measures:"+s);
					particleFilter.resample(Z);
				} catch (Exception ex)
				{
					System.out.println(ex.getMessage());
				}
				pfTest.this.repaint();
				System.out.println("target:"+x + ", " + y);
			}
		});
		
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);

		JLabel lblDistance = new JLabel("Distance");
		textFieldDistance = new JTextField();
		textFieldDistance.setText("1");
		textFieldDistance.setColumns(10);

		JLabel lblTurndeg = new JLabel("Turn(deg)");
		textFieldTurn = new JTextField();
		textFieldTurn.setText("0");
		textFieldTurn.setColumns(10);

		JButton btnMove = new JButton("Move");
		btnMove.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				float move = Float.parseFloat(textFieldDistance.getText());
				float turn = (float) Math.toRadians(Float.parseFloat(textFieldTurn.getText()));
				try
				{
					targetParticle.move(turn, move);
					particleFilter.move(turn, move);
					particleFilter.resample(targetParticle.sense());
				} catch (Exception ex)
				{
					System.out.println(ex.getMessage());
				}
				pfTest.this.repaint();
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
		
		JTextArea txtrReaPointLandmarks = new JTextArea();
		txtrReaPointLandmarks.setText("Notice:\n Red:Target Position\n Pink:particles\n Yellow:Landmarks(Base station)\n Blue:Expected a posteriori(EAP)\n Green:Maximized a posteriori(MAP)");

		GroupLayout gl_contentPane = new GroupLayout(contentPane);
		gl_contentPane.setHorizontalGroup(
			gl_contentPane.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_contentPane.createSequentialGroup()
					.addContainerGap(714, Short.MAX_VALUE)
					.addGroup(gl_contentPane.createParallelGroup(Alignment.LEADING)
						.addGroup(Alignment.TRAILING, gl_contentPane.createParallelGroup(Alignment.LEADING, false)
							.addComponent(btnMove, Alignment.TRAILING, GroupLayout.PREFERRED_SIZE, 93, GroupLayout.PREFERRED_SIZE)
							.addComponent(btnReset, Alignment.TRAILING, GroupLayout.PREFERRED_SIZE, 93, GroupLayout.PREFERRED_SIZE)
							.addComponent(txtrReaPointLandmarks, Alignment.TRAILING))
						.addGroup(Alignment.TRAILING, gl_contentPane.createParallelGroup(Alignment.LEADING)
							.addGroup(gl_contentPane.createSequentialGroup()
								.addComponent(lblDistance, GroupLayout.PREFERRED_SIZE, 54, GroupLayout.PREFERRED_SIZE)
								.addGap(2)
								.addComponent(textFieldDistance, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
							.addGroup(gl_contentPane.createSequentialGroup()
								.addComponent(lblTurndeg)
								.addGap(2)
								.addComponent(textFieldTurn, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))))
					.addContainerGap())
		);
		gl_contentPane.setVerticalGroup(
			gl_contentPane.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_contentPane.createSequentialGroup()
					.addGroup(gl_contentPane.createParallelGroup(Alignment.LEADING)
						.addGroup(gl_contentPane.createSequentialGroup()
							.addGap(3)
							.addComponent(lblDistance))
						.addComponent(textFieldDistance, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
					.addGap(14)
					.addGroup(gl_contentPane.createParallelGroup(Alignment.LEADING)
						.addGroup(gl_contentPane.createSequentialGroup()
							.addGap(3)
							.addComponent(lblTurndeg))
						.addComponent(textFieldTurn, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(btnMove)
					.addGap(18)
					.addComponent(btnReset)
					.addGap(32)
					.addComponent(txtrReaPointLandmarks, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
					.addContainerGap(219, Short.MAX_VALUE))
		);
		contentPane.setLayout(gl_contentPane);
	}
}
