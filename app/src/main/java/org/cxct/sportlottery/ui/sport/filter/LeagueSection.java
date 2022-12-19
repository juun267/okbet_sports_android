package org.cxct.sportlottery.ui.sport.filter;


import com.chad.library.adapter.base.entity.SectionEntity;

import org.cxct.sportlottery.network.league.League;


public class LeagueSection implements SectionEntity {

    private boolean isHeader;
    public League t;
    public String header;

    public LeagueSection(boolean isHeader, String header) {
        this.isHeader = isHeader;
        this.header = header;
        this.t = null;
    }

    public LeagueSection(League league) {
        this.isHeader = false;
        this.header = null;
        this.t = league;
    }

    @Override
    public boolean isHeader() {
        return isHeader;
    }

}
