package npc;

/*
 *
 *
 * @author EMTI
 */

import player.Player;

public interface IAtionNpc {

    void openBaseMenu(Player player);

    void confirmMenu(Player player, int select);

}
