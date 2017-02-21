<html>
    <head>
        <title>Monitor</title>
        <link href="/bootstrap-3.3.7-dist/css/bootstrap.css" rel="stylesheet" />
    </head>
    <body>
  <#list monitorMap?values as nodeDetail>
    ${nodeDetail.nodeId}
  </#list> 
</body>
</html>