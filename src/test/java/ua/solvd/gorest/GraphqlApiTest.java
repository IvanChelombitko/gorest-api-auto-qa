package ua.solvd.gorest;

import org.testng.Assert;
import org.testng.annotations.Test;
import tools.jackson.databind.JsonNode;
import ua.solvd.gorest.model.ApiResponse;
import ua.solvd.gorest.model.GraphqlRequest;
import ua.solvd.gorest.model.GraphqlResponseDto;
import ua.solvd.gorest.model.PostPayload;
import ua.solvd.gorest.util.PayloadTemplate;

public class GraphqlApiTest extends BaseTest {

    @Test(description = "TC-011")
    public void testGetUsersListGraphql() {
        String query = "query { users { nodes { id name email } } }";
        GraphqlRequest request = new GraphqlRequest(query);
        ApiResponse<GraphqlResponseDto> response = graphqlService.sendQuery(request);
        Assert.assertEquals(response.getStatusCode(), 200, "Status code should be 200.");
        Assert.assertNotNull(response.getBody().data(), "Data object should not be null.");
        JsonNode usersNode = response.getBody().data().get("users").get("nodes");
        Assert.assertNotNull(usersNode, "Users nodes should not be null.");
        Assert.assertTrue(usersNode.isArray(), "Nodes should be an array.");
        Assert.assertTrue(usersNode.size() > 0, "Users array should not be empty.");
    }

    @Test(description = "TC-012")
    public void testGetUserByIdGraphql() {
        int userId = getTemporaryUserId();
        String query = String.format("query { user(id: %d) { id name status } }", userId);
        GraphqlRequest request = new GraphqlRequest(query);
        ApiResponse<GraphqlResponseDto> response = graphqlService.sendQuery(request);
        Assert.assertEquals(response.getStatusCode(), 200, "Status code should be 200.");
        Assert.assertNotNull(response.getBody().data(), "Data object should not be null.");
        JsonNode userNode = response.getBody().data().get("user");
        Assert.assertNotNull(userNode, "User object should not be null.");
        Assert.assertEquals(userNode.get("id").asInt(), userId, "User ID does not match.");
    }

    @Test(description = "TC-013")
    public void testGetUserWithPostsGraphql() {
        int userId = getTemporaryUserId();
        PostPayload postPayload = PayloadTemplate.getValidPost(userId);
        ApiResponse<PostPayload> createPostResponse = apiService.createPost(userId, postPayload);
        Assert.assertEquals(createPostResponse.getStatusCode(), 201, "Precondition failed: Post was not created.");
        String query = String.format("query { user(id: %d) { id name posts { nodes { title body } } } }", userId);
        GraphqlRequest request = new GraphqlRequest(query);
        ApiResponse<GraphqlResponseDto> response = graphqlService.sendQuery(request);
        Assert.assertEquals(response.getStatusCode(), 200, "Status code should be 200.");
        Assert.assertNotNull(response.getBody().data(), "Data object should not be null.");
        JsonNode userNode = response.getBody().data().get("user");
        Assert.assertNotNull(userNode, "User object should not be null.");
        JsonNode postsNode = userNode.get("posts").get("nodes");
        Assert.assertNotNull(postsNode, "Posts nodes should not be null.");
        Assert.assertTrue(postsNode.isArray(), "Posts nodes should be an array.");
        Assert.assertTrue(postsNode.size() > 0, "Posts array should not be empty.");
        Assert.assertEquals(postsNode.get(0).get("title").asString(), postPayload.title(), "Post title does not match.");
    }

    @Test(description = "TC-014")
    public void testGraphqlSyntaxError() {
        String query = "query { nonExistentFieldStructure { invalidParam } }";
        GraphqlRequest request = new GraphqlRequest(query);
        ApiResponse<GraphqlResponseDto> response = graphqlService.sendQuery(request);
        Assert.assertEquals(response.getStatusCode(), 200, "Status code should be 200 for GraphQL payload error.");
        Assert.assertNotNull(response.getBody().errors(), "Errors array should be present in response.");
        Assert.assertTrue(response.getBody().errors().isArray(), "Errors should be an array.");
        Assert.assertTrue(response.getBody().errors().size() > 0, "Errors array should not be empty.");
    }

    @Test(description = "TC-015")
    public void testUnauthorizedGraphqlRequest() {
        String query = "query { users { nodes { id } } }";
        GraphqlRequest request = new GraphqlRequest(query);
        ApiResponse<GraphqlResponseDto> response = graphqlService.sendUnauthorizedQuery(request);
        Assert.assertEquals(response.getStatusCode(), 401, "Status code should be 401.");
        Assert.assertEquals(response.getBody().message(), "Authentication failed", "Error message mismatch.");
    }
}