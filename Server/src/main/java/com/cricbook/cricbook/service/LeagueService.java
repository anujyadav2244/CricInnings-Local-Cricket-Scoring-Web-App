package com.cricbook.cricbook.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.cricbook.cricbook.model.League;
import com.cricbook.cricbook.repository.LeagueRepository;

@Service
public class LeagueService {

    @Autowired
    private LeagueRepository leagueRepository;

    public League createLeague(League league) {
        return leagueRepository.save(league);
    }

    public Optional<League> getLeagueById(String id) {
        return leagueRepository.findById(id);
    }

    public Optional<League> getLeagueByName(String name) {
        return leagueRepository.findByName(name);
    }

    public List<League> getAllLeagues() {
        return leagueRepository.findAll();
    }

    public void deleteLeagueById(String id) {
        leagueRepository.deleteById(id);
    }

    public void deleteLeagueByName(String name) {
        leagueRepository.findByName(name).ifPresent(league -> leagueRepository.delete(league));
    }
}
