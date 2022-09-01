package org.cxct.sportlottery.ui.sport;

import com.chad.library.adapter.base.entity.SectionEntity;

import org.cxct.sportlottery.network.league.League;


public class LeagueSection extends SectionEntity<League> {
    public LeagueSection(boolean isHeader, String header) {
        super(isHeader, header);
    }

    public LeagueSection(League league) {
        super(league);
    }
}
