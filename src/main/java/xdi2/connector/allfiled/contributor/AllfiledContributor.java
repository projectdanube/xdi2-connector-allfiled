package xdi2.connector.allfiled.contributor;

import java.io.IOException;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import xdi2.connector.allfiled.api.AllfiledApi;
import xdi2.connector.allfiled.mapping.AllfiledMapping;
import xdi2.connector.allfiled.util.GraphUtil;
import xdi2.core.ContextNode;
import xdi2.core.Graph;
import xdi2.core.xri3.impl.XRI3Segment;
import xdi2.messaging.GetOperation;
import xdi2.messaging.MessageEnvelope;
import xdi2.messaging.MessageResult;
import xdi2.messaging.exceptions.Xdi2MessagingException;
import xdi2.messaging.target.ExecutionContext;
import xdi2.messaging.target.contributor.AbstractContributor;
import xdi2.messaging.target.contributor.ContributorCall;
import xdi2.messaging.target.interceptor.MessageEnvelopeInterceptor;

public class AllfiledContributor extends AbstractContributor implements MessageEnvelopeInterceptor {

	private static final Logger log = LoggerFactory.getLogger(AllfiledContributor.class);

	private Graph tokenGraph;
	private AllfiledApi allfiledApi;
	private AllfiledMapping allfiledMapping;

	public AllfiledContributor() {

		super();

		this.getContributors().addContributor(new AllfiledUserContributor());
	}

	/*
	 * MessageEnvelopeInterceptor
	 */

	@Override
	public boolean before(MessageEnvelope messageEnvelope, MessageResult messageResult, ExecutionContext executionContext) throws Xdi2MessagingException {

		AllfiledContributorExecutionContext.resetUsers(executionContext);

		return false;
	}

	@Override
	public boolean after(MessageEnvelope messageEnvelope, MessageResult messageResult, ExecutionContext executionContext) throws Xdi2MessagingException {

		return false;
	}

	@Override
	public void exception(MessageEnvelope messageEnvelope, MessageResult messageResult, ExecutionContext executionContext, Exception ex) {

	}

	/*
	 * Sub-Contributors
	 */

	@ContributorCall(addresses={"($)"})
	private class AllfiledUserContributor extends AbstractContributor {

		private AllfiledUserContributor() {

			super();

			this.getContributors().addContributor(new AllfiledUserAttributeContributor());
		}
	}

	@ContributorCall(addresses={"($)"})
	private class AllfiledUserAttributeContributor extends AbstractContributor {

		private AllfiledUserAttributeContributor() {

			super();
		}

		@Override
		public boolean getContext(XRI3Segment[] contributorXris, XRI3Segment relativeContextNodeXri, XRI3Segment contextNodeXri, GetOperation operation, MessageResult messageResult, ExecutionContext executionContext) throws Xdi2MessagingException {

			XRI3Segment allfiledContextXri = contributorXris[contributorXris.length - 3];
			XRI3Segment userXri = contributorXris[contributorXris.length - 2];
			XRI3Segment allfiledDataXri = contributorXris[contributorXris.length - 1];

			log.debug("allfiledContextXri: " + allfiledContextXri + ", userXri: " + userXri + ", allfiledDataXri: " + allfiledDataXri);

			// retrieve the Allfiled value

			String allfiledValue = null;

			try {

				String allfiledFieldIdentifier = AllfiledContributor.this.allfiledMapping.allfiledDataXriToAllfiledFieldIdentifier(allfiledDataXri);
				if (allfiledFieldIdentifier == null) return false;

				String accessToken = GraphUtil.retrieveAccessToken(AllfiledContributor.this.getTokenGraph(), userXri);
				if (accessToken == null) throw new Exception("No access token.");

				JSONObject user = AllfiledContributor.this.retrieveUser(executionContext, accessToken);
				if (user == null) throw new Exception("No user.");
				if (! user.has(allfiledFieldIdentifier)) return false;

				allfiledValue = user.getString(allfiledFieldIdentifier);
			} catch (Exception ex) {

				throw new Xdi2MessagingException("Cannot load user data: " + ex.getMessage(), ex, null);
			}

			// add the Allfiled value to the response

			if (allfiledValue != null) {

				ContextNode contextNode = messageResult.getGraph().findContextNode(contextNodeXri, true);
				contextNode.createLiteral(allfiledValue);
			}

			// done

			return true;
		}
	}

	/*
	 * Helper methods
	 */

	private JSONObject retrieveUser(ExecutionContext executionContext, String accessToken) throws IOException, JSONException {

		JSONObject user = AllfiledContributorExecutionContext.getUser(executionContext, accessToken);

		if (user == null) {

			user = this.allfiledApi.getUser(accessToken);
			AllfiledContributorExecutionContext.putUser(executionContext, accessToken, user);
		}

		return user;
	}

	/*
	 * Getters and setters
	 */

	public Graph getTokenGraph() {

		return this.tokenGraph;
	}

	public void setTokenGraph(Graph tokenGraph) {

		this.tokenGraph = tokenGraph;
	}

	public AllfiledApi getAllfiledApi() {

		return this.allfiledApi;
	}

	public void setAllfiledApi(AllfiledApi templateApi) {

		this.allfiledApi = templateApi;
	}

	public AllfiledMapping getAllfiledMapping() {

		return this.allfiledMapping;
	}

	public void setAllfiledMapping(AllfiledMapping allfiledMapping) {

		this.allfiledMapping = allfiledMapping;
	}
}
