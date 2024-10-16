package example.todolist.api;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import social.nickrest.http.HttpStatus;
import social.nickrest.http.data.Context;
import social.nickrest.http.data.Mapping;
import social.nickrest.http.method.MethodEnum;
import social.nickrest.http.request.IRequest;
import social.nickrest.http.request.IResponse;
import social.nickrest.util.NumberUtil;

import java.util.ArrayList;
import java.util.List;

@Context
public class TestRequest {

    private final List<String> todo = new ArrayList<>();

    @Mapping(path = "/todo", contentType = "application/json", method = MethodEnum.GET)
    public JsonArray todo() {
        JsonArray jsonArray = new JsonArray();
        todo.forEach(jsonArray::add);

        return jsonArray;
    }

    @Mapping(path = "/addTodo", contentType = "application/json", method = MethodEnum.GET)
    public JsonObject setTodo(IRequest request) {
        JsonObject returnObject = new JsonObject();

        String toDo = request.query().get("todo");

        if (toDo == null) {
            request.status(HttpStatus.BAD_REQUEST);

            returnObject.addProperty("error", "todo is required");
            return returnObject;
        }

        todo.add(toDo);

        request.status(HttpStatus.OK);
        returnObject.addProperty("status", HttpStatus.OK.getReasonPhrase());
        return returnObject;
    }

    @Mapping(path = "/removeTodo", contentType = "application/json", method = MethodEnum.GET)
    public JsonArray removeTodo(IRequest request) {
        JsonArray returnObject = new JsonArray();

        if(request.query().get("todo") != null && NumberUtil.isInteger(request.query().get("todo"))) {
            int toDo = Integer.parseInt(request.query().get("todo"));

            if(toDo < 0 || toDo >= todo.size()) {
                request.status(HttpStatus.BAD_REQUEST);
                return returnObject;
            }

            todo.remove(toDo);
            request.status(HttpStatus.OK);
            returnObject.forEach(returnObject::add);
            return returnObject;
        }

        request.status(HttpStatus.BAD_REQUEST);
        return returnObject;
    }

}
