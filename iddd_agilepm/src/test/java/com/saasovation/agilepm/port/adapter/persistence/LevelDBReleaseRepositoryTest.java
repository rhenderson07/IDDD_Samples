//   Copyright 2012,2013 Vaughn Vernon
//
//   Licensed under the Apache License, Version 2.0 (the "License");
//   you may not use this file except in compliance with the License.
//   You may obtain a copy of the License at
//
//       http://www.apache.org/licenses/LICENSE-2.0
//
//   Unless required by applicable law or agreed to in writing, software
//   distributed under the License is distributed on an "AS IS" BASIS,
//   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//   See the License for the specific language governing permissions and
//   limitations under the License.

package com.saasovation.agilepm.port.adapter.persistence;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.junit.Test;

import com.saasovation.agilepm.domain.model.product.ProductId;
import com.saasovation.agilepm.domain.model.product.release.Release;
import com.saasovation.agilepm.domain.model.product.release.ReleaseId;
import com.saasovation.agilepm.domain.model.product.release.ReleaseRepository;
import com.saasovation.agilepm.domain.model.tenant.TenantId;
import com.saasovation.common.port.adapter.persistence.leveldb.LevelDBUnitOfWork;

public class LevelDBReleaseRepositoryTest extends BaseLevelDBRepositoryTest {

    private ReleaseRepository releaseRepository = new LevelDBReleaseRepository();

    @Test
    public void testSave()  {
        Release release = new Release(
                new TenantId("12345"), new ProductId("p00000"), new ReleaseId("r11111"),
                "release1", "My release 1.", new Date(), new Date());

        LevelDBUnitOfWork.start(super.getDB());
        releaseRepository.save(release);
        LevelDBUnitOfWork.current().commit();

        Release savedRelease = releaseRepository.releaseOfId(release.tenantId(), release.releaseId());

        assertNotNull(savedRelease);
        assertEquals(release.tenantId(), savedRelease.tenantId());
        assertEquals(release.name(), savedRelease.name());

        Collection<Release> savedReleases =
                this.releaseRepository.allProductReleases(release.tenantId(), release.productId());

        assertFalse(savedReleases.isEmpty());
        assertEquals(1, savedReleases.size());
    }

    @Test
    public void testRemove() {
        Release release1 = new Release(
                new TenantId("12345"), new ProductId("p00000"), new ReleaseId("r11111"),
                "release1", "My release 1.", new Date(), new Date());

        Release release2 = new Release(
                new TenantId("12345"), new ProductId("p00000"), new ReleaseId("r11112"),
                "release2", "My release 2.", new Date(), new Date());

        LevelDBUnitOfWork.start(super.getDB());
        releaseRepository.save(release1);
        releaseRepository.save(release2);
        LevelDBUnitOfWork.current().commit();

        LevelDBUnitOfWork.start(super.getDB());
        releaseRepository.remove(release1);
        LevelDBUnitOfWork.current().commit();

        TenantId tenantId = release2.tenantId();
        ProductId productId = release2.productId();

        Collection<Release> savedReleases = releaseRepository.allProductReleases(tenantId, productId);
        assertFalse(savedReleases.isEmpty());
        assertEquals(1, savedReleases.size());
        assertEquals(release2.name(), savedReleases.iterator().next().name());

        LevelDBUnitOfWork.start(super.getDB());
        releaseRepository.remove(release2);
        LevelDBUnitOfWork.current().commit();

        savedReleases = releaseRepository.allProductReleases(tenantId, productId);
        assertTrue(savedReleases.isEmpty());
    }

    @Test
    public void testSaveAllRemoveAll()  {
        Release release1 = new Release(
                new TenantId("12345"), new ProductId("p00000"), new ReleaseId("r11111"),
                "release1", "My release 1.", new Date(), new Date());

        Release release2 = new Release(
                new TenantId("12345"), new ProductId("p00000"), new ReleaseId("r11112"),
                "release2", "My release 2.", new Date(), new Date());

        Release release3 = new Release(
                new TenantId("12345"), new ProductId("p00000"), new ReleaseId("r11113"),
                "release3", "My release 3.", new Date(), new Date());

        LevelDBUnitOfWork.start(super.getDB());
        releaseRepository.saveAll(Arrays.asList(new Release[] { release1, release2, release3 }));
        LevelDBUnitOfWork.current().commit();

        TenantId tenantId = release1.tenantId();
        ProductId productId = release1.productId();

        Collection<Release> savedReleases = releaseRepository.allProductReleases(tenantId, productId);
        assertFalse(savedReleases.isEmpty());
        assertEquals(3, savedReleases.size());

        LevelDBUnitOfWork.start(super.getDB());
        releaseRepository.removeAll(Arrays.asList(new Release[] { release1, release3 }));
        LevelDBUnitOfWork.current().commit();

        savedReleases = releaseRepository.allProductReleases(tenantId, productId);
        assertFalse(savedReleases.isEmpty());
        assertEquals(1, savedReleases.size());
        assertEquals(release2.name(), savedReleases.iterator().next().name());

        LevelDBUnitOfWork.start(super.getDB());
        releaseRepository.removeAll(Arrays.asList(new Release[] { release2 }));
        LevelDBUnitOfWork.current().commit();

        savedReleases = releaseRepository.allProductReleases(tenantId, productId);
        assertTrue(savedReleases.isEmpty());
    }

    @Test
    public void testConcurrentTransactions()  {
        final List<Integer> orderOfCommits = new ArrayList<Integer>();

        Release release1 = new Release(
                new TenantId("12345"), new ProductId("p00000"), new ReleaseId("r11111"),
                "release1", "My release 1.", new Date(), new Date());

        LevelDBUnitOfWork.start(super.getDB());
        releaseRepository.save(release1);

        new Thread() {
           @Override
           public void run() {
               Release release2 = new Release(
                       new TenantId("12345"), new ProductId("p00000"), new ReleaseId("r11112"),
                       "release2", "My release 2.", new Date(), new Date());

               LevelDBUnitOfWork.start(getDB());
               releaseRepository.save(release2);
               LevelDBUnitOfWork.current().commit();
               orderOfCommits.add(2);
           }
        }.start();

        try {
			Thread.sleep(250L);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

        LevelDBUnitOfWork.current().commit();
        orderOfCommits.add(1);

        for (int idx = 0; idx < orderOfCommits.size(); ++idx) {
            assertEquals(idx + 1, orderOfCommits.get(idx).intValue());
        }

        try {
			Thread.sleep(250L);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

        Collection<Release> savedReleases = releaseRepository.allProductReleases(release1.tenantId(), release1.productId());

        assertFalse(savedReleases.isEmpty());
        assertEquals(2, savedReleases.size());
    }

}
