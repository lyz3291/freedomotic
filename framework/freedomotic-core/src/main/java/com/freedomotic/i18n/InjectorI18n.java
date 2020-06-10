/**
 *
 * Copyright (c) 2009-2020 Freedomotic Team http://freedomotic.com
 *
 * This file is part of Freedomotic
 *
 * This Program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2, or (at your option) any later version.
 *
 * This Program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * Freedomotic; see the file COPYING. If not, see
 * <http://www.gnu.org/licenses/>.
 */
package com.freedomotic.i18n;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;

/**
 * Guice module for support i18n logic
 * 
 * @author Enrico Nicoletti
 */
public class InjectorI18n extends AbstractModule {

    @Override
    protected void configure() {
        bind(I18n.class).to(I18nImpl.class).in(Singleton.class);
    }

}
