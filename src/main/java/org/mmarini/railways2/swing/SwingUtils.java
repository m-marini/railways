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

package org.mmarini.railways2.swing;

import javax.swing.*;
import java.text.MessageFormat;
import java.util.Optional;

/**
 * Swing utility functions
 */
public interface SwingUtils {

    /**
     * Returns the initialized button
     *
     * @param key the message key
     */
    static JButton createButton(String key) {
        JButton button = new JButton(Messages.getString(key + ".name"));

        Messages.getStringOpt(key + ".icon")
                .flatMap(s -> Optional.ofNullable(UIController.class.getResource(s)))
                .map(ImageIcon::new)
                .ifPresent(button::setIcon);
        Messages.getStringOpt(key + ".mnemonic")
                .map(s -> s.charAt(0))
                .ifPresent(button::setMnemonic);
        Messages.getStringOpt(key + ".tip")
                .ifPresent(button::setToolTipText);
        Messages.getStringOpt(key + ".selectedIcon")
                .flatMap(s -> Optional.ofNullable(UIController.class.getResource(s)))
                .map(ImageIcon::new)
                .ifPresent(button::setSelectedIcon);
        return button;
    }

    /**
     * Returns the initialized checkbox menu item
     *
     * @param key the message key
     */
    static JCheckBoxMenuItem createCheckBoxMenuItem(String key) {
        String name = Messages.getString(key + ".name");
        JCheckBoxMenuItem menu = new JCheckBoxMenuItem(name);
        Messages.getStringOpt(key + ".mnemonic")
                .map(s -> s.charAt(0))
                .ifPresent(menu::setMnemonic);
        Messages.getStringOpt(key + ".accelerator")
                .map(KeyStroke::getKeyStroke)
                .ifPresent(menu::setAccelerator);
        Messages.getStringOpt(key + ".tip")
                .ifPresent(menu::setToolTipText);
        Messages.getStringOpt(key + ".icon")
                .flatMap(s -> Optional.ofNullable(UIController.class.getResource(s)))
                .map(ImageIcon::new)
                .ifPresent(menu::setIcon);
        Messages.getStringOpt(key + ".selectedIcon")
                .flatMap(s -> Optional.ofNullable(UIController.class.getResource(s)))
                .map(ImageIcon::new)
                .ifPresent(menu::setSelectedIcon);
        return menu;
    }

    /**
     * Returns the initialized menu item
     *
     * @param key the message key
     */
    static JMenuItem createMenuItem(String key) {
        String name = Messages.getString(key + ".name");
        JMenuItem menu = new JMenuItem(name);
        Messages.getStringOpt(key + ".mnemonic")
                .map(s -> s.charAt(0))
                .ifPresent(menu::setMnemonic);
        Messages.getStringOpt(key + ".accelerator")
                .map(KeyStroke::getKeyStroke)
                .ifPresent(menu::setAccelerator);
        Messages.getStringOpt(key + ".tip")
                .ifPresent(menu::setToolTipText);
        Messages.getStringOpt(key + ".icon")
                .flatMap(s -> Optional.ofNullable(UIController.class.getResource(s)))
                .map(ImageIcon::new)
                .ifPresent(menu::setIcon);
        Messages.getStringOpt(key + ".selectedIcon")
                .flatMap(s -> Optional.ofNullable(UIController.class.getResource(s)))
                .map(ImageIcon::new)
                .ifPresent(menu::setSelectedIcon);
        return menu;
    }

    /**
     * Returns the initialized toolbar button
     *
     * @param key the message key
     */
    static JButton createToolBarButton(String key) {
        JButton button = new JButton();
        Messages.getStringOpt(key + ".icon")
                .flatMap(s -> Optional.ofNullable(UIController.class.getResource(s)))
                .map(ImageIcon::new)
                .ifPresentOrElse(button::setIcon,
                        () -> button.setText(Messages.getString(key + ".name")));

        Messages.getStringOpt(key + ".mnemonic")
                .map(s -> s.charAt(0))
                .ifPresent(button::setMnemonic);
        Messages.getStringOpt(key + ".tip")
                .ifPresent(button::setToolTipText);
        Messages.getStringOpt(key + ".selectedIcon")
                .flatMap(s -> Optional.ofNullable(UIController.class.getResource(s)))
                .map(ImageIcon::new)
                .ifPresent(button::setSelectedIcon);
        return button;
    }

    /**
     * Returns the initialized toolbar toggle button
     *
     * @param key the message key
     */
    static JToggleButton createToolBarToggleButton(String key) {
        JToggleButton button = new JToggleButton();
        Messages.getStringOpt(key + ".icon")
                .flatMap(s -> Optional.ofNullable(UIController.class.getResource(s)))
                .map(ImageIcon::new)
                .ifPresentOrElse(button::setIcon,
                        () -> button.setText(Messages.getString(key + ".name")));

        Messages.getStringOpt(key + ".mnemonic")
                .map(s -> s.charAt(0))
                .ifPresent(button::setMnemonic);
        Messages.getStringOpt(key + ".tip")
                .ifPresent(button::setToolTipText);
        Messages.getStringOpt(key + ".selectedIcon")
                .flatMap(s -> Optional.ofNullable(UIController.class.getResource(s)))
                .map(ImageIcon::new)
                .ifPresent(button::setSelectedIcon);
        return button;
    }

    /**
     * Returns the formatted text by the message key
     *
     * @param messageKey the formatting message key
     * @param args       the arguments
     */
    static String formatMessage(String messageKey, Object... args) {
        return MessageFormat.format(Messages.getString(messageKey), args);
    }

    /**
     * Returns true if the confirm dialog has been closed b confirmatin (OK button)
     *
     * @param titleKey the title key message
     * @param content  the content
     */
    static boolean showConfirmDialog(String titleKey, JComponent content) {
        int selectedValue = JOptionPane.showConfirmDialog(null, content, Messages.getString(titleKey),
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        return selectedValue == JOptionPane.OK_OPTION;
    }

    /**
     * Shows a message dialog
     *
     * @param titleKey the title key message
     * @param content  the content
     */
    static void showMessageKey(String titleKey, JComponent content) {
        JOptionPane.showMessageDialog(null, content, Messages.getString(titleKey),
                JOptionPane.PLAIN_MESSAGE);
    }
}
