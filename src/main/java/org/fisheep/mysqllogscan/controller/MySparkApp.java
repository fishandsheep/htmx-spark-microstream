package org.fisheep.mysqllogscan.controller;

import one.microstream.storage.embedded.types.EmbeddedStorage;
import one.microstream.storage.embedded.types.EmbeddedStorageManager;
import org.fisheep.mysqllogscan.bean.MyRoot;
import org.fisheep.mysqllogscan.bean.User;
import spark.ModelAndView;
import spark.template.thymeleaf.ThymeleafTemplateEngine;
import spark.utils.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static spark.Spark.*;

public class MySparkApp {


    public static void main(String[] args) {

        staticFiles.location("/static");

        final EmbeddedStorageManager storage = EmbeddedStorage.start();
        //监听服务关闭的钩子
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            storage.shutdown();
        }));

        MyRoot root = null;
        if (storage.root() == null) {
            root = new MyRoot();
            storage.setRoot(root);
            storage.storeRoot();
        } else {
            root = (MyRoot) storage.root();

        }

        get("/", (req, res) -> {
            Map<String, Object> model = new HashMap<>();
            //加载数据
            return render(model, "index");

        });

        MyRoot finalRoot = root;
        get("/getAllUsers", (req, res) -> {
            Map<String, Object> model = new HashMap<>();
            model.put("users", finalRoot.users);
            return render(model, "users");

        });

        put("/addUser", (req, res) -> {
            Map<String, Object> model = new HashMap<>();
            //保存
            User user = User.builder()
                    .name(req.queryParams("name"))
                    .nickName(req.queryParams("nickName"))
                    .mobile(req.queryParams("mobile"))
                    .email(req.queryParams("email"))
                    .build();
            finalRoot.users.add(0, user);
            storage.store(finalRoot.users);
            //返回给前端
            model.put("name", user.getName());
            model.put("nickName", user.getNickName());
            model.put("mobile", user.getMobile());
            model.put("email", user.getEmail());
            return render(model, "user");
        });

        post("/searchName", (req, res) -> {
            String searchValue = req.queryParams("searchKey");
            Map<String, Object> model = new HashMap<>();
            if (StringUtils.isNotBlank(searchValue)) {
                List<User> searchUsers = finalRoot.users.stream()
                        .filter(s -> s.getName().contains(searchValue))
                        .collect(Collectors.toList());
                model.put("users", searchUsers);
            } else {
                model.put("users", finalRoot.users);
            }
            return render(model, "users");
        });

    }

    public static String render(Map<String, Object> model, String templatePath) {
        return new ThymeleafTemplateEngine().render(new ModelAndView(model, templatePath));
    }

}
