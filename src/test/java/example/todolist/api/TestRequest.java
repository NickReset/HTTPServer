package example.todolist.api;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import social.nickrest.http.HttpStatus;
import social.nickrest.http.data.Context;
import social.nickrest.http.data.Mapping;
import social.nickrest.http.method.MethodEnum;
import social.nickrest.http.request.IResponse;
import social.nickrest.util.NumberUtil;

import java.util.ArrayList;
import java.util.List;

@Context
public class TestRequest {

    private final List<String> todo = new ArrayList<>();

    @Mapping(path = "/todo", contentType = "application/json", method = MethodEnum.GET)
    public JsonArray todo(IResponse response) {
        JsonArray jsonArray = new JsonArray();
        todo.forEach(jsonArray::add);

        return jsonArray;
    }

    @Mapping(path = "/addTodo", contentType = "application/json", method = MethodEnum.GET)
    public JsonObject setTodo(IResponse response) {
        JsonObject returnObject = new JsonObject();

        String toDo = response.query().get("todo");

        if(toDo == null) {
            response.status(HttpStatus.BAD_REQUEST);

            returnObject.addProperty("error", "todo is required");
            return returnObject;
        }

        todo.add(toDo);

        response.status(HttpStatus.OK);
        returnObject.addProperty("status", HttpStatus.OK.getReasonPhrase());
        return returnObject;
    }

    @Mapping(path = "/removeTodo", contentType = "application/json", method = MethodEnum.GET)
    public JsonArray removeTodo(IResponse response) {
        JsonArray returnObject = new JsonArray();

        if(response.query().get("todo") != null && NumberUtil.isInteger(response.query().get("todo"))) {
            int toDo = Integer.parseInt(response.query().get("todo"));

            if(toDo < 0 || toDo >= todo.size()) {
                response.status(HttpStatus.BAD_REQUEST);
                return returnObject;
            }

            todo.remove(toDo);
            response.status(HttpStatus.OK);
            returnObject.forEach(returnObject::add);
            return returnObject;
        }

        response.status(HttpStatus.BAD_REQUEST);
        return returnObject;
    }

}
