<html>
    <head>
    	<#if refreshPage>
    	    <meta http-equiv="refresh" content="10" />
    	</#if>
        <title>Monitor</title>
        <link href="/bootstrap-3.3.7-dist/css/bootstrap.css" rel="stylesheet" />
        <script src="/chart.js-2.7.2/dist/Chart.bundle.js"></script>
        <script src="/chart.js-2.7.2/dist/utils.js"></script>
    </head>
    <body>
    
		<div id="canvas-holder" style="width:40%">
			<canvas id="chart-area"></canvas>
		</div>
	<script>
		var config = {
			type: 'doughnut',
			data: {
				datasets: [{
					data: [
						<#list monitorMap?values as workerDetail>
						${workerDetail.totalExecutionsWithoutComma},
						</#list>
					],
					backgroundColor: [
						window.chartColors.red,
						window.chartColors.green,
						window.chartColors.yellow,
						window.chartColors.purple	,
						window.chartColors.blue,
						window.chartColors.grey,
						window.chartColors.orange,
					],
					label: 'Workers Monitor'
				}],
				labels: [
					<#list monitorMap?values as workerDetail>
					'${workerDetail.inetAddres}:${workerDetail.inetPort}',
					</#list>
				]
			},
			options: {
				responsive: true
			}
		};

		window.onload = function() {
			var ctx = document.getElementById('chart-area').getContext('2d');
			window.myPie = new Chart(ctx, config);
		};
	</script>
	
  <table class="table table-condensed">
    <thead>
      <tr>
      	<th>Status</th>
        <th>Address:Port</th>
        <th>Start Time</th>
        <th>End Time</th>
        <th>Elapsed Time</th>
        <th>Max Pool Size</th>
        <th># Processed</th>
        <th>Avg process time</th>
      </tr>
    </thead>
    <tbody>
<#assign totalExecuted = 0>
<#assign totalWorkers = 0>
<#assign totalThreads = 0>
<#assign averageExecutionTime = 0>
<#list monitorMap?values as workerDetail>
	<#assign totalExecuted = totalExecuted + workerDetail.totalExecutions>
	<#assign totalWorkers = totalWorkers + 1>
	<#assign totalThreads = totalThreads + workerDetail.poolMaxSize>
	<#assign averageExecutionTime = averageExecutionTime + workerDetail.avgExecutionTime>
		<tr>
			<td>${workerDetail.activeStatus?c}</td>
			<td>${workerDetail.inetAddres}:${workerDetail.inetPort}</td>
			<td>${workerDetail.startTimeString}</td>
			<td>${workerDetail.stopTimeString}</td>
			<td>${workerDetail.totalElapsedTime}</td>
			<td>${workerDetail.poolMaxSize}</td>
			<td>${workerDetail.totalExecutions}</td>
			<td>${workerDetail.avgExecutionTime}</td>
		</tr>
</#list>
<#if totalWorkers gt 0>
  <#assign averageExecutionTime = averageExecutionTime / totalWorkers>
</#if>

		<tr>
			<td><b>${totalWorkers}</b></td>
			<td colspan="4">&nbsp;</td>
			<td><b>${totalThreads}</b></td>
			<td><b>${totalExecuted}</b></td>
			<td><b>${averageExecutionTime}</b></td>
		</tr>
		<tr><td colspan="8">&nbsp;</td></tr>
    </tbody>
  </table>
</body>
</html>