package com.tdsoft.bro.core.exception.handler;

import java.util.AbstractMap.SimpleEntry;
import java.util.LinkedList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.TypeMismatchException;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.RedisSystemException;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.transaction.CannotCreateTransactionException;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.TransactionTimedOutException;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.HttpMediaTypeException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.sns.model.AuthorizationErrorException;
import com.amazonaws.services.sns.model.EndpointDisabledException;
import com.amazonaws.services.sns.model.InternalErrorException;
import com.amazonaws.services.sns.model.InvalidParameterException;
import com.amazonaws.services.sns.model.InvalidParameterValueException;
import com.amazonaws.services.sns.model.NotFoundException;
import com.amazonaws.services.sns.model.PlatformApplicationDisabledException;
import com.amazonaws.services.sns.model.SubscriptionLimitExceededException;
import com.amazonaws.services.sns.model.TopicLimitExceededException;
import com.amazonaws.services.sqs.model.BatchEntryIdsNotDistinctException;
import com.amazonaws.services.sqs.model.BatchRequestTooLongException;
import com.amazonaws.services.sqs.model.EmptyBatchRequestException;
import com.amazonaws.services.sqs.model.InvalidAttributeNameException;
import com.amazonaws.services.sqs.model.InvalidBatchEntryIdException;
import com.amazonaws.services.sqs.model.InvalidIdFormatException;
import com.amazonaws.services.sqs.model.InvalidMessageContentsException;
import com.amazonaws.services.sqs.model.MessageNotInflightException;
import com.amazonaws.services.sqs.model.OverLimitException;
import com.amazonaws.services.sqs.model.QueueDeletedRecentlyException;
import com.amazonaws.services.sqs.model.QueueDoesNotExistException;
import com.amazonaws.services.sqs.model.QueueNameExistsException;
import com.amazonaws.services.sqs.model.ReceiptHandleIsInvalidException;
import com.amazonaws.services.sqs.model.TooManyEntriesInBatchRequestException;
import com.fasterxml.jackson.core.JsonParseException;
import com.google.common.base.Optional;
import com.tdsoft.bro.core.exception.ArgumentInvalidException;
import com.tdsoft.bro.core.exception.BroException;
import com.tdsoft.bro.core.exception.DeviceNotFoundException;
import com.tdsoft.bro.core.exception.JsonFormatException;
import com.tdsoft.bro.core.exception.code.BusinessErrorCode;
import com.tdsoft.bro.core.exception.code.CacheErrorCode;
import com.tdsoft.bro.core.exception.code.DatabaseErrorCode;
import com.tdsoft.bro.core.exception.code.ErrorCode;
import com.tdsoft.bro.core.exception.code.NotificationErrorCode;
import com.tdsoft.bro.core.exception.code.QueueErrorCode;
import com.tdsoft.bro.core.exception.code.SystemErrorCode;

/**
 * @author Daniel
 *
 */
@ControllerAdvice
public class BroExceptionHandler {

	private static final Logger broLogger = LoggerFactory.getLogger(BroExceptionHandler.class);

	// ///////////////////////////////////////////////
	//
	// Business
	//
	// ///////////////////////////////////////////////

	@ExceptionHandler(value = DeviceNotFoundException.class)
	@ResponseStatus(HttpStatus.UNAUTHORIZED)
	@ResponseBody
	public ExceptionResponse deviceErrorHandler(HttpServletRequest req, DeviceNotFoundException e) throws Exception {
		return handleCommonsError(req, e, BusinessErrorCode.B0201);
	}

	@ExceptionHandler(value = HttpRequestMethodNotSupportedException.class)
	@ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
	@ResponseBody
	public ExceptionResponse httpMethodErrorHandler(HttpServletRequest req, Exception e) throws Exception {
		return handleCommonsError(req, e, BusinessErrorCode.B0102);
	}

	@ExceptionHandler(value = {HttpMessageNotReadableException.class, JsonFormatException.class, ArgumentInvalidException.class, JsonParseException.class})
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	@ResponseBody
	public ExceptionResponse httpMessageReadingErrorHandler(HttpServletRequest req, Exception e) throws Exception {
		return handleCommonsError(req, e, BusinessErrorCode.B0101);
	}

	@ExceptionHandler(value = {TypeMismatchException.class})
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	@ResponseBody
	public ExceptionResponse httpMessageArgumentsTypeErrorHandler(HttpServletRequest req, TypeMismatchException e) throws Exception {
		ExceptionResponse response = handleCommonsError(req, e, BusinessErrorCode.B0101);
		StringBuilder sb = new StringBuilder();
		sb.append(e.getErrorCode()).append(", Value:").append(e.getValue());
		response.setErrorMessage(sb.toString());
		return response;
	}

	@ExceptionHandler(value = {MethodArgumentNotValidException.class})
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	@ResponseBody
	public ExceptionResponse httpMessageArgumentsNotValidHandler(HttpServletRequest req, MethodArgumentNotValidException e)
			throws Exception {
		ExceptionResponse response = handleCommonsError(req, e, BusinessErrorCode.B0101);
		List<SimpleEntry<String, String>> errorMessages = new LinkedList<SimpleEntry<String, String>>();
		List<ObjectError> allErrors = e.getBindingResult().getAllErrors();
		for (ObjectError err : allErrors) {
			String name;
			if (err instanceof FieldError) {
				name = ((FieldError) err).getField();
			} else {
				name = err.getObjectName();
			}
			String message = err.getDefaultMessage();
			SimpleEntry<String, String> pair = new SimpleEntry<String, String>(name, message);
			errorMessages.add(pair);
		}
		response.setErrorMessage(errorMessages);
		return response;
	}

	@ExceptionHandler(value = {HttpMediaTypeException.class})
	@ResponseStatus(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
	@ResponseBody
	public ExceptionResponse httpMediaTypeErrorHandler(HttpServletRequest req, Exception e) throws Exception {
		return handleCommonsError(req, e, BusinessErrorCode.B0101);
	}

	@ExceptionHandler(value = BroException.class)
	@ResponseBody
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public ExceptionResponse broErrorHandler(HttpServletRequest req, BroException e) throws Exception {
		return handleCommonsError(req, e, BusinessErrorCode.B0001);
	}

	// ///////////////////////////////////////////////
	//
	// Cache
	//
	// ///////////////////////////////////////////////

	@ExceptionHandler(value = RedisConnectionFailureException.class)
	@ResponseBody
	@ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
	public ExceptionResponse cacheConnectionErrorHandler(HttpServletRequest req, RedisConnectionFailureException e) throws Exception {
		return handleCommonsError(req, e, CacheErrorCode.S0202);
	}

	@ExceptionHandler(value = RedisSystemException.class)
	@ResponseBody
	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	public ExceptionResponse cacheSystemErrorHandler(HttpServletRequest req, RedisSystemException e) throws Exception {
		return handleCommonsError(req, e, CacheErrorCode.S0201);
	}

	// ///////////////////////////////////////////////
	//
	// Queue
	//
	// ///////////////////////////////////////////////

	@ExceptionHandler(value = {BatchEntryIdsNotDistinctException.class, BatchRequestTooLongException.class,
			EmptyBatchRequestException.class, InvalidBatchEntryIdException.class, TooManyEntriesInBatchRequestException.class})
	@ResponseBody
	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	public ExceptionResponse queueBatchArgumentsErrorHandler(HttpServletRequest req, AmazonServiceException e) throws Exception {
		return handleCommonsError(req, e, QueueErrorCode.S0304);
	}

	@ExceptionHandler(value = {InvalidAttributeNameException.class, InvalidIdFormatException.class, InvalidMessageContentsException.class,
			QueueNameExistsException.class, MessageNotInflightException.class})
	@ResponseBody
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public ExceptionResponse queueArgumentsErrorHandler(HttpServletRequest req, AmazonServiceException e) throws Exception {
		return handleCommonsError(req, e, QueueErrorCode.S0303);
	}

	@ExceptionHandler(value = {QueueDoesNotExistException.class, QueueDeletedRecentlyException.class})
	@ResponseBody
	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	public ExceptionResponse queueTargetHandler(HttpServletRequest req, AmazonServiceException e) throws Exception {
		return handleCommonsError(req, e, QueueErrorCode.S0302);
	}

	@ExceptionHandler(value = {UnsupportedOperationException.class, OverLimitException.class, ReceiptHandleIsInvalidException.class})
	@ResponseBody
	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	public ExceptionResponse queueErrorHandler(HttpServletRequest req, AmazonServiceException e) throws Exception {
		return handleCommonsError(req, e, QueueErrorCode.S0301);
	}

	// ///////////////////////////////////////////////
	//
	// Notification
	//
	// ///////////////////////////////////////////////

	@ExceptionHandler(value = {InvalidParameterValueException.class, InvalidParameterException.class})
	@ResponseBody
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public ExceptionResponse notificationArgumentsErrorHandler(HttpServletRequest req, AmazonServiceException e) throws Exception {
		return handleCommonsError(req, e, NotificationErrorCode.S0404);
	}

	@ExceptionHandler(value = {EndpointDisabledException.class, NotFoundException.class})
	@ResponseBody
	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	public ExceptionResponse notificationTargetErrorHandler(HttpServletRequest req, AmazonServiceException e) throws Exception {
		return handleCommonsError(req, e, NotificationErrorCode.S0403);
	}

	@ExceptionHandler(value = AuthorizationErrorException.class)
	@ResponseBody
	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	public ExceptionResponse notificationAuthErrorHandler(HttpServletRequest req, AuthorizationErrorException e) throws Exception {
		return handleCommonsError(req, e, NotificationErrorCode.S0402);
	}

	@ExceptionHandler(value = {InternalErrorException.class, PlatformApplicationDisabledException.class,
			SubscriptionLimitExceededException.class, TopicLimitExceededException.class})
	@ResponseBody
	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	public ExceptionResponse notificationErrorHandler(HttpServletRequest req, AmazonServiceException e) throws Exception {
		return handleCommonsError(req, e, NotificationErrorCode.S0401);
	}

	// ///////////////////////////////////////////////
	//
	// Database
	//
	// ///////////////////////////////////////////////

	@ExceptionHandler(value = DataIntegrityViolationException.class)
	@ResponseBody
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public ExceptionResponse databaseConstraintErrorHandler(HttpServletRequest req, DataIntegrityViolationException e) throws Exception {
		return handleCommonsError(req, e, DatabaseErrorCode.S0105);
	}

	@ExceptionHandler(value = DuplicateKeyException.class)
	@ResponseBody
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public ExceptionResponse databaseDuplicateKeyErrorHandler(HttpServletRequest req, DuplicateKeyException e) throws Exception {
		return handleCommonsError(req, e, DatabaseErrorCode.S0104);
	}


	@ExceptionHandler(value = TransactionTimedOutException.class)
	@ResponseBody
	@ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
	public ExceptionResponse databaseTxTimoutErrorHandler(HttpServletRequest req, TransactionTimedOutException e) throws Exception {
		return handleCommonsError(req, e, DatabaseErrorCode.S0103);
	}

	@ExceptionHandler(value = DataAccessResourceFailureException.class)
	@ResponseBody
	@ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
	public ExceptionResponse databaseJDBCConnectionErrorHandler(HttpServletRequest req, DataAccessResourceFailureException e)
			throws Exception {
		return handleCommonsError(req, e, DatabaseErrorCode.S0102);
	}

	@ExceptionHandler(value = CannotCreateTransactionException.class)
	@ResponseBody
	@ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
	public ExceptionResponse databaseJPAConnectionErrorHandler(HttpServletRequest req, CannotCreateTransactionException e) throws Exception {
		return handleCommonsError(req, e, DatabaseErrorCode.S0102);
	}

	// ///////////////////////////////////////////////
	//
	// System
	//
	// ///////////////////////////////////////////////

	@ExceptionHandler(value = AmazonClientException.class)
	@ResponseBody
	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	public ExceptionResponse amazonServiceErrorHandler(HttpServletRequest req, AmazonClientException e) throws Exception {
		return handleCommonsError(req, e, SystemErrorCode.S0002);
	}

	@ExceptionHandler(value = Exception.class)
	@ResponseBody
	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	public ExceptionResponse defaultErrorHandler(HttpServletRequest req, Exception e) throws Exception {
		if (AnnotationUtils.findAnnotation(e.getClass(), ResponseStatus.class) != null)
			throw e;
		broLogger.error("", e);
		return handleCommonsError(req, e, SystemErrorCode.S0001);
	}

	// ///////////////////////////////////////////////
	//
	// Commons
	//
	// ///////////////////////////////////////////////

	@ExceptionHandler(value = DataAccessException.class)
	@ResponseBody
	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	public ExceptionResponse dataAccessErrorHandler(HttpServletRequest req, DataAccessException e) throws Exception {
		return handleCommonsError(req, e, SystemErrorCode.S0001);
	}

	@ExceptionHandler(value = TransactionException.class)
	@ResponseBody
	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	public ExceptionResponse transactionErrorHandler(HttpServletRequest req, TransactionException e) throws Exception {
		return handleCommonsError(req, e, SystemErrorCode.S0001);
	}

	private ExceptionResponse handleCommonsError(HttpServletRequest req, Exception e, ErrorCode code) throws Exception {
		ExceptionResponse response = new ExceptionResponse();
		response.setErrorCode(code.getCode());
		response.setErrorMessage(code.getCauseMessage());
		response.setUrl(req.getRequestURL().toString());
		if (Optional.fromNullable(e.getCause()).isPresent()) {
			response.setDetails(e.getCause().getMessage());
		} else {
			response.setDetails(e.getMessage());
		}

		return response;
	}
}
