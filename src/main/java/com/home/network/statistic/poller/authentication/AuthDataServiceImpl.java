package com.home.network.statistic.poller.authentication;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Profile({"dev-admin", "prd-admin"})
@Service
@RequiredArgsConstructor
public class AuthDataServiceImpl implements AuthDataService {
	private final AuthDataRepo authDataRepo;

    @Override
    @Transactional("appJpaTx")
    public void upsertAuthData(AuthData authData) {
        authDataRepo.upsert(authData);
    }

    @Override
    @Transactional(value = "appJpaTx", readOnly = true)
    public AuthData findById(Integer id) {
        if (id == null) {
            return new AuthData();
        }

        var idOpt = authDataRepo.findById(id);
        return idOpt.orElseGet(AuthData::new);
    }

    @Override
    @Transactional(value = "appJpaTx", readOnly = true)
    public List<AuthData> findAll(Integer page, Integer limit) {
        return authDataRepo.findAll(page, limit);
    }

    @Override
    @Transactional("appJpaTx")
    public boolean deleteById(Integer id) {
        return Optional.ofNullable(id).map(authDataRepo::deleteById).orElse(false);
    }

    @Override
	@Transactional(value = "appJpaTx", readOnly = true)
	public long countResultAll() {
		return authDataRepo.countResultAll();
	}
    
}
