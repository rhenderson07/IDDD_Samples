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

package com.saasovation.agilepm.application.product;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.Date;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

import com.saasovation.agilepm.AgilePmTestConfig;
import com.saasovation.agilepm.domain.model.discussion.DiscussionAvailability;
import com.saasovation.agilepm.domain.model.product.Product;
import com.saasovation.agilepm.domain.model.product.ProductId;
import com.saasovation.agilepm.domain.model.product.ProductRepository;
import com.saasovation.agilepm.domain.model.team.ProductOwner;
import com.saasovation.agilepm.domain.model.team.ProductOwnerId;
import com.saasovation.agilepm.domain.model.tenant.TenantId;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(loader = AnnotationConfigContextLoader.class, classes = AgilePmTestConfig.class)
public class ProductApplicationServiceTest // extends
											// ProductApplicationCommonTest
{
	@Autowired
	private ProductRepository productRepository;

	@Autowired
	private ProductApplicationService productApplicationService;

	private Product product;
	private ProductOwner productOwner;

	@Before
	public void setUp() {
		product = getMockProduct();
		productOwner = getMockProductOwner();
	}

	private Product getMockProduct() {
		TenantId tenantId = new TenantId("T12345");
		return new Product(tenantId, new ProductId("P12345"), new ProductOwnerId(tenantId, "zdoe"), "My Product",
				"This is the description of my product.", DiscussionAvailability.NOT_REQUESTED);

	}

	private ProductOwner getMockProductOwner() {
		return new ProductOwner(new TenantId("T-12345"), "zoe", "Zoe", "Doe", "zoe@saasovation.com",
				new Date(new Date().getTime() - (86400000L * 30)));
	}

	@Test
	public void testDiscussionProcess() {
		this.productApplicationService.requestProductDiscussion(
				new RequestProductDiscussionCommand(product.tenantId().id(), product.productId().id()));

		this.productApplicationService.startDiscussionInitiation(
				new StartDiscussionInitiationCommand(product.tenantId().id(), product.productId().id()));

		Product productWithStartedDiscussionInitiation = this.productRepository.productOfId(product.tenantId(),
				product.productId());

		assertNotNull(productWithStartedDiscussionInitiation.discussionInitiationId());

		String discussionId = UUID.randomUUID().toString().toUpperCase();

		InitiateDiscussionCommand command = new InitiateDiscussionCommand(product.tenantId().id(),
				product.productId().id(), discussionId);

		this.productApplicationService.initiateDiscussion(command);

		Product productWithInitiatedDiscussion = this.productRepository.productOfId(product.tenantId(),
				product.productId());

		assertEquals(discussionId, productWithInitiatedDiscussion.discussion().descriptor().id());
	}

	@Test
	public void testNewProduct() {
		String productOwnerIdStr = productOwner.productOwnerId().id();
		NewProductCommand newProductCommand = new NewProductCommand("T-12345", productOwnerIdStr, "My Product",
				"The description of My Product.");

		String newProductIdStr = this.productApplicationService.newProduct(newProductCommand);

		ProductId newProductId = new ProductId(newProductIdStr);
		Product newProduct = this.productRepository.productOfId(productOwner.tenantId(), newProductId);

		assertNotNull(newProduct);
		assertEquals("My Product", newProduct.name());
		assertEquals("The description of My Product.", newProduct.description());
	}

	@Test
	public void testNewProductWithDiscussion() {
		String productOwnerIdStr = productOwner.productOwnerId().id();
		NewProductCommand newProductCommand = new NewProductCommand("T-12345", productOwnerIdStr, "My Product",
				"The description of My Product.");
		String newProductId = this.productApplicationService.newProductWithDiscussion(newProductCommand);

		Product newProduct = this.productRepository.productOfId(productOwner.tenantId(), new ProductId(newProductId));

		assertNotNull(newProduct);
		assertEquals("My Product", newProduct.name());
		assertEquals("The description of My Product.", newProduct.description());
		assertEquals(DiscussionAvailability.REQUESTED, newProduct.discussion().availability());
	}

	@Test
	public void testRequestProductDiscussion() {
		this.productApplicationService.requestProductDiscussion(
				new RequestProductDiscussionCommand(product.tenantId().id(), product.productId().id()));

		Product productWithRequestedDiscussion = this.productRepository.productOfId(product.tenantId(),
				product.productId());

		assertEquals(DiscussionAvailability.REQUESTED, productWithRequestedDiscussion.discussion().availability());
	}

	@Test
	public void testRetryProductDiscussionRequest() {
		this.productApplicationService.requestProductDiscussion(
				new RequestProductDiscussionCommand(product.tenantId().id(), product.productId().id()));

		Product productWithRequestedDiscussion = this.productRepository.productOfId(product.tenantId(),
				product.productId());

		assertEquals(DiscussionAvailability.REQUESTED, productWithRequestedDiscussion.discussion().availability());

		this.productApplicationService.startDiscussionInitiation(
				new StartDiscussionInitiationCommand(product.tenantId().id(), product.productId().id()));

		Product productWithDiscussionInitiation = this.productRepository.productOfId(product.tenantId(),
				product.productId());

		assertNotNull(productWithDiscussionInitiation.discussionInitiationId());

		this.productApplicationService.retryProductDiscussionRequest(new RetryProductDiscussionRequestCommand(
				product.tenantId().id(), productWithDiscussionInitiation.discussionInitiationId()));

		Product productWithRetriedRequestedDiscussion = this.productRepository.productOfId(product.tenantId(),
				product.productId());

		assertEquals(DiscussionAvailability.REQUESTED,
				productWithRetriedRequestedDiscussion.discussion().availability());
	}

	@Test
	public void testStartDiscussionInitiation() {
		this.productApplicationService.requestProductDiscussion(
				new RequestProductDiscussionCommand(product.tenantId().id(), product.productId().id()));

		Product productWithRequestedDiscussion = this.productRepository.productOfId(product.tenantId(),
				product.productId());

		assertEquals(DiscussionAvailability.REQUESTED, productWithRequestedDiscussion.discussion().availability());

		assertNull(productWithRequestedDiscussion.discussionInitiationId());

		this.productApplicationService.startDiscussionInitiation(
				new StartDiscussionInitiationCommand(product.tenantId().id(), product.productId().id()));

		Product productWithDiscussionInitiation = this.productRepository.productOfId(product.tenantId(),
				product.productId());

		assertNotNull(productWithDiscussionInitiation.discussionInitiationId());
	}

	@Test
	public void testTimeOutProductDiscussionRequest() {
		// TODO: student assignment
	}
}
