<html>
    <head>
        <title>Monitor</title>
        <link href="/bootstrap-3.3.7-dist/css/bootstrap.css" rel="stylesheet" />
    </head>
    <body>
  <table class="table table-condensed">
    <thead>
      <tr>
        <th>NodeId</th>
        <th>Address</th>
        <th>Port</th>
        <th>End Time</th>
        <th># Processed</th>
      </tr>
    </thead>
    <tbody>
<#list monitorMap?values as nodeDetail>
      <tr>
        <td>${nodeDetail.nodeId}</td>
        <td>${nodeDetail.inetAddres}</td>
        <td>${nodeDetail.inetPort}</td>
        <td>${nodeDetail.stopTimeString}</td>
        <td>${nodeDetail.elapsedArraySize}</td>
      </tr>
</#list> 
    </tbody>
  </table>
</body>
</html>