package com.xyz.booking.service;

import com.xyz.booking.client.PaymentServiceClient;
import com.xyz.booking.client.TheatreServiceClient;
import com.xyz.booking.dto.*;
import com.xyz.booking.entity.Booking;
import com.xyz.booking.entity.BookingSeat;
import com.xyz.booking.entity.BookingStatus;
import com.xyz.booking.exception.BookingServiceException;
import com.xyz.booking.offer.BookingContext;
import com.xyz.booking.offer.DiscountResult;
import com.xyz.booking.offer.OfferEngine;
import com.xyz.booking.repository.BookingRepository;
import com.xyz.common.events.BookingConfirmedEvent;
import lombok.Required