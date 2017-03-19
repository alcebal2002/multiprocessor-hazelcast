<html>
    <head>
    	<meta http-equiv="refresh" content="10" />
        <title>Monitor</title>
        <link href="/bootstrap-3.3.7-dist/css/bootstrap.css" rel="stylesheet" />
    </head>
    <body>
  <table class="table table-condensed">
    <thead>
      <tr>
      	<th>Status</th>
        <th>Address</th>
        <th>Port</th>
        <th>Start Time</th>
        <th>End Time</th>
        <th># Processed</th>
        <th>Avg process time</th>
      </tr>
    </thead>
    <tbody>
    	<tr>
    		<td colspan="7"><b>Active</b></td>
    	</tr>
<#assign totalActive = 0>
<#assign totalInactive = 0>
<#list monitorMap?values as nodeDetail>
	<#if nodeDetail.activeStatus>
	<#assign totalActive = totalActive + nodeDetail.elapsedArraySize>
      <tr>
      	<td>${nodeDetail.activeStatus?c}</td>
        <td>${nodeDetail.inetAddres}</td>
        <td>${nodeDetail.inetPort}</td>
        <td>${nodeDetail.startTimeString}</td>
        <td>${nodeDetail.stopTimeString}</td>
        <td>${nodeDetail.elapsedArraySize}</td>
        <td>${nodeDetail.avgElapsedTime}</td>
      </tr>
	</#if>
</#list>
		<tr>
    		<td colspan="7">${totalActive}</td>
    	</tr>
    	<tr>
    		<td colspan="7"><b>Inactive</b></td>
    	</tr>
<#list monitorMap?values as nodeDetail>
	<#if !nodeDetail.activeStatus>
	<#assign totalInactive = totalInactive + nodeDetail.elapsedArraySize>
      <tr>
      	<td>${nodeDetail.activeStatus?c}</td>
        <td>${nodeDetail.inetAddres}</td>
        <td>${nodeDetail.inetPort}</td>
        <td>${nodeDetail.startTimeString}</td>
        <td>${nodeDetail.stopTimeString}</td>
        <td>${nodeDetail.elapsedArraySize}</td>
        <td>${nodeDetail.avgElapsedTime}</td>
      </tr>
	</#if>
</#list> 
		<tr>
    		<td colspan="7">${totalInactive}</td>
    	</tr>
    </tbody>
  </table>
</body>
</html>