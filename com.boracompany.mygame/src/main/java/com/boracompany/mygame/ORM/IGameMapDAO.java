package com.boracompany.mygame.ORM;

import com.boracompany.mygame.Model.GameMap;
import com.boracompany.mygame.Model.Player;

import java.util.List;

public interface IGameMapDAO {

    void save(GameMap gameMap);

    GameMap findById(Long id);

    List<GameMap> findAll();

    void update(GameMap gameMap);

    void delete(Long id);

    List<Player> findPlayersByMapId(Long mapId);

    void addPlayerToMap(Long mapId, Player player);

    void removePlayerFromMap(Long mapId, Player player);
}