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
import xdi2.core.xri3.impl.XDI3Segment;
import xdi2.messaging.GetOperation;
import xdi2.messaging.MessageEnvelope;
import xdi2.messaging.MessageResult;
import xdi2.messaging.exceptions.Xdi2MessagingException;
import xdi2.messaging.target.ExecutionContext;
import xdi2.messaging.target.MessagingTarget;
import xdi2.messaging.target.Prototype;
import xdi2.messaging.target.contributor.AbstractContributor;
import xdi2.messaging.target.contributor.ContributorXri;
import xdi2.messaging.target.impl.graph.GraphMessagingTarget;
import xdi2.messaging.target.interceptor.MessageEnvelopeInterceptor;
import xdi2.messaging.target.interceptor.MessagingTargetInterceptor;

@ContributorXri(addresses={"+(https://allfiled.com/)"})
public class AllfiledContributor extends AbstractContributor implements MessagingTargetInterceptor, MessageEnvelopeInterceptor, Prototype<AllfiledContributor> {

	private static final Logger log = LoggerFactory.getLogger(AllfiledContributor.class);

	private Graph tokenGraph;
	private AllfiledApi allfiledApi;
	private AllfiledMapping allfiledMapping;

	public AllfiledContributor() {

		super();

		this.getContributors().addContributor(new AllfiledEnabledContributor());
		this.getContributors().addContributor(new AllfiledUserContributor());
	}

	/*
	 * Prototype
	 */

	@Override
	public AllfiledContributor instanceFor(PrototypingContext prototypingContext) throws Xdi2MessagingException {

		// create new contributor

		AllfiledContributor contributor = new AllfiledContributor();

		// set api and mapping

		contributor.setAllfiledApi(this.getAllfiledApi());
		contributor.setAllfiledMapping(this.getAllfiledMapping());

		// done

		return contributor;
	}

	/*
	 * MessagingTargetInterceptor
	 */

	@Override
	public void init(MessagingTarget messagingTarget) throws Exception {

		// set the token graph

		if (this.tokenGraph == null && messagingTarget instanceof GraphMessagingTarget) {

			this.setTokenGraph(((GraphMessagingTarget) messagingTarget).getGraph());
		}
	}

	@Override
	public void shutdown(MessagingTarget messagingTarget) throws Exception {

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

	@ContributorXri(addresses={"$!(+enabled)"})
	private class AllfiledEnabledContributor extends AbstractContributor {

		@Override
		public boolean getContext(XDI3Segment[] contributorXris, XDI3Segment relativeContextNodeXri, XDI3Segment contextNodeXri, GetOperation operation, MessageResult messageResult, ExecutionContext executionContext) throws Xdi2MessagingException {

			messageResult.getGraph().findContextNode(contextNodeXri, true).createLiteral("1");

			return true;
		}
	}

	@ContributorXri(addresses={"($$!)"})
	private class AllfiledUserContributor extends AbstractContributor {

		private AllfiledUserContributor() {

			super();

			this.getContributors().addContributor(new AllfiledCategoryFileFieldContributor());
		}
	}

	@ContributorXri(addresses={"($)($)($)"})
	private class AllfiledCategoryFileFieldContributor extends AbstractContributor {

		private AllfiledCategoryFileFieldContributor() {

			super();
		}

		@Override
		public boolean getContext(XDI3Segment[] contributorXris, XDI3Segment relativeContextNodeXri, XDI3Segment contextNodeXri, GetOperation operation, MessageResult messageResult, ExecutionContext executionContext) throws Xdi2MessagingException {

			XDI3Segment allfiledContextXri = contributorXris[contributorXris.length - 3];
			XDI3Segment userXri = contributorXris[contributorXris.length - 2];
			XDI3Segment allfiledDataXri = contributorXris[contributorXris.length - 1];

			log.debug("allfiledContextXri: " + allfiledContextXri + ", userXri: " + userXri + ", allfiledDataXri: " + allfiledDataXri);

			// retrieve the Allfiled value

			String allfiledValue = null;

			try {

				String allfiledCategoryIdentifier = AllfiledContributor.this.allfiledMapping.allfiledDataXriToAllfiledCategoryIdentifier(allfiledDataXri);
				String allfiledFileIdentifier = AllfiledContributor.this.allfiledMapping.allfiledDataXriToAllfiledFileIdentifier(allfiledDataXri);
				String allfiledFieldIdentifier = AllfiledContributor.this.allfiledMapping.allfiledDataXriToAllfiledFieldIdentifier(allfiledDataXri);
				if (allfiledCategoryIdentifier == null) return false;
				if (allfiledFileIdentifier == null) return false;
				if (allfiledFieldIdentifier == null) return false;

				log.debug("allfiledCategoryIdentifier: " + allfiledCategoryIdentifier + ", allfiledFileIdentifier: " + allfiledFileIdentifier + ", allfiledFieldIdentifier: " + allfiledFieldIdentifier);
				
				String accessToken = GraphUtil.retrieveAccessToken(AllfiledContributor.this.getTokenGraph(), userXri);
				if (accessToken == null) throw new Exception("No access token.");

				JSONObject file = AllfiledContributor.this.retrieveFile(executionContext, accessToken, allfiledCategoryIdentifier, allfiledFileIdentifier);
				if (file == null) throw new Exception("No user.");
				if (! file.getJSONObject("fieldsMap").has(allfiledFieldIdentifier)) return false;

				allfiledValue = file.getJSONObject("fieldsMap").getString(allfiledFieldIdentifier);
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

	private JSONObject retrieveFile(ExecutionContext executionContext, String accessToken, String categoryIdentifier, String fileIdentifier) throws IOException, JSONException {

		JSONObject user = AllfiledContributorExecutionContext.getUser(executionContext, accessToken);

		if (user == null) {

			user = this.allfiledApi.getFile(accessToken, categoryIdentifier, fileIdentifier);
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
