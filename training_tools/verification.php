<?php

$testFileFormat = 'loc%d_trainset.txt.scale'; // 테스트 파일 포맷
$modelFileForamt = 'model%d'; // 모델 파일 포맷
$outputFile = 'output'; // 아웃풋 파일이름

/* 셋트가 몇개있는지 */
$testFileSet = [1,2,3,4,5];
$modelFileSet = [1,2,3,4,5];

$dirPath = '/Users/madcat/Desktop/trainset';
$outputFilePath = sprintf('%s/%s', $dirPath, $outputFile);

/* SVM 명령어 */
$svmPredictLocation = '/Users/madcat/Downloads/libsvm-3.21/';
$svmPredictCommand = 'svm-predict %s %s %s'; // 순서 : 테스트파일, 모델파일, 아웃풋파일

/*
	각 테스트 파일을 읽어서 한줄씩 읽어냄 (한줄 = 하나의 테스트 데이터)
	하나의 테스트 데이터마다 각 모델과의 유사도를 계산하여
	가장 큰 유사도를 갖는 (가장큰값) 모델을 카운팅함
*/


$count_testFileSet = count($testFileSet);
$count_modelFileSeet = count($modelFileSet);

for($i = 0; $i < $count_testFileSet; $i++) {

	$testFileName = sprintf($testFileFormat, $testFileSet[$i]);
	$testFilePath = sprintf('%s/%s', $dirPath, $testFileName);

	echo ' VERIFICATION FILE :: ' . $testFilePath . PHP_EOL;

	$testFileData = file($testFilePath);
	
	$resultArray = array();

	foreach($testFileData as $line) {
		$tempFilePath = sprintf('%s/%s', $dirPath, 'testTemp');
		file_put_contents($tempFilePath, $line);

		// 최대값을 갖는 인덱스
		$_max = -999999999;
		$_maxModelNum = 0;

		foreach($modelFileSet as $modelNum) {
			$modelFileName = sprintf($modelFileForamt, $modelNum);
			$modelFilePath = sprintf('%s/%s', $dirPath, $modelFileName);

			$convertedSvmPredictCommand = sprintf($svmPredictCommand, $tempFilePath, $modelFilePath, $outputFilePath);
			$execCommand = sprintf('%s%s', $svmPredictLocation, $convertedSvmPredictCommand);
			$output = array();

			exec($execCommand, $output);
			$result = trim($output[0]);
			$regex = '/SUM ::::: (-?\d*\.\d*)/';
			$match = array();
			preg_match($regex, $result, $match);

			// SUM값 추출
			$extracted = (double) $match[1];

			// 최대값을 갖는 모델을 찾기위함
			if( $extracted > $_max) {
				$_max = $extracted;
				$_maxModelNum = $modelNum;
			}
		}

		/* 최대값을 갖는 모델에 카운팅 올려줌 */
		$resultArray[$_maxModelNum]++;

	}

	foreach($resultArray as $modelNum => $counted) {
		echo sprintf('Model:%d  COUNT : %d', $modelNum, $counted) . PHP_EOL;
	}
	echo PHP_EOL;


}