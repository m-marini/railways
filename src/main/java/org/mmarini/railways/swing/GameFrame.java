/*
 * Copyright (c) 2023  Marco Marini, marco.marini@mmarini.org
 *
 * Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without
 * restriction, including without limitation the rights to use,
 * copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following
 * conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 *
 *    END OF TERMS AND CONDITIONS
 *
 */

package org.mmarini.railways.swing;

import org.mmarini.railways.model.GameHandlerImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.net.URL;
import java.text.MessageFormat;

public class GameFrame extends JFrame implements LookAndFeelListener {
    private static final String IMAGE_RESOURCE_NAME = "org/mmarini/railways/swing/railways.png";

    private static final long serialVersionUID = 1L;

    private static final Logger logger = LoggerFactory.getLogger(GameFrame.class);

    /**
     * @param args the arguments
     */
    public static void main(String[] args) {
        logger.info("Starting Railways ...");
        GameFrame frame = new GameFrame();
        frame.setVisible(true);
    }

    private final MainPane mainPane;
    private final ActionHandler actionsHandler;

    /**
     *
     */
    public GameFrame() {
        mainPane = new MainPane();
        actionsHandler = new ActionHandler();
        GameHandlerImpl gameHandler = new GameHandlerImpl();
        mainPane.setGameHandler(gameHandler);
        actionsHandler.setGameHandler(gameHandler);
        actionsHandler.setGameFrame(this);
        actionsHandler.init();
        init();
    }

    /**
     *
     */
    public boolean handleAboutAction() {
        return mainPane.handleAboutAction();
    }

    /**
     *
     */
    public boolean handleExitAction() {
        mainPane.handleExitAction();
        dispose();
        return true;
    }

    /**
     * @see org.mmarini.railways.swing.LookAndFeelListener#handleLAFChanged()
     */
    @Override
    public void handleLAFChanged() {
        SwingUtilities.updateComponentTreeUI(this);
        mainPane.handleLAFChanged();
    }

    /**
     *
     */
    public boolean handleNewGameAction() {
        return mainPane.handleNewGameAction();
    }

    /**
     *
     */
    public boolean handleOptionsAction() {
        return mainPane.handleOptionsAction();
    }

    /**
     *
     */
    public void init() {
        logger.debug("init");
        Container contentPane = getContentPane();
        contentPane.setLayout(new BorderLayout());

        MainPane mainPane = this.mainPane;
        contentPane.add(actionsHandler.getGameToolBar(), BorderLayout.NORTH);
        contentPane.add(mainPane, BorderLayout.CENTER);

        setJMenuBar(actionsHandler.getGameMenuBar());

        String title = Messages.getString("GameFrame.title"); //$NON-NLS-1$
        title = MessageFormat
                .format(title,
                        Messages.getString("Railways.name"), Messages.getString("Railways.version"), //$NON-NLS-1$ //$NON-NLS-2$
                        Messages.getString("Railways.author")); //$NON-NLS-1$
        setTitle(title);
        URL imgResource = Thread.currentThread().getContextClassLoader()
                .getResource(IMAGE_RESOURCE_NAME);
        ImageIcon imageIcon = new ImageIcon(imgResource);
        setIconImage(imageIcon.getImage());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        setSize(screen);
        setLocation(0, 0);
        actionsHandler.setLookAndFeelListener(this);
        actionsHandler.reload();
    }
}