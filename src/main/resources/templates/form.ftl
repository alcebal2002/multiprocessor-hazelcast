<html>
    <head>
        <title>Welcome</title>
        <link href="/bootstrap-3.3.7-dist/css/bootstrap.css" rel="stylesheet" />
    </head>
    <body>
        <form class="form-inline" method="POST" action="/test_freemarker_result">
            <div class="form-group">
                <label for="name">Name</label>
                <input type="text"
                       class="form-control"
                       id="name"
                       name="name"
                       placeholder="John Doe">
            </div>
            <div class="form-group">
                <label for="email">Email</label>
                <input type="email"
                       class="form-control"
                       id="email"
                       name="email"
                       placeholder="john.doe@example.org">
            </div>
            <button type="submit" class="btn btn-default">Send invitation</button>
        </form>
    <body>
</html>