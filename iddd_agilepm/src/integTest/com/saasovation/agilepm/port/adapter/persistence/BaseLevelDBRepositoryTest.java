package com.saasovation.agilepm.port.adapter.persistence;

import org.iq80.leveldb.DB;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

import com.saasovation.agilepm.AgilePmTestConfig;
import com.saasovation.common.domain.model.DomainEventPublisher;
import com.saasovation.common.port.adapter.persistence.leveldb.LevelDBProvider;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(loader = AnnotationConfigContextLoader.class, classes = AgilePmTestConfig.class)
public abstract class BaseLevelDBRepositoryTest {
	@Autowired
	private LevelDBProvider dbProvider;

	@Autowired
	private DB database;
	
	@Before
	public void setUp() {
		DomainEventPublisher.instance().reset();
		dbProvider.purge(database);
	}
	
	protected DB getDB(){
		return database;
	}
}
